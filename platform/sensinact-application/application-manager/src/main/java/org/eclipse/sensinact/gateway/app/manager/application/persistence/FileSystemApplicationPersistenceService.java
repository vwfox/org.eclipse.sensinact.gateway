/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.app.manager.application.persistence;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.sensinact.gateway.app.api.persistence.ApplicationPersistenceService;
import org.eclipse.sensinact.gateway.app.api.persistence.dao.Application;
import org.eclipse.sensinact.gateway.app.api.persistence.exception.ApplicationPersistenceException;
import org.eclipse.sensinact.gateway.app.api.persistence.listener.ApplicationAvailabilityListener;
import org.eclipse.sensinact.gateway.app.manager.application.persistence.exception.ApplicationParseException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.component.propertytypes.ServiceRanking;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.json.JsonObject;

@Component(service = ApplicationPersistenceService.class,scope = ServiceScope.SINGLETON)
@ServiceRanking(100)
@Designate(ocd = FileSystemApplicationPersistenceService.Config.class)
public class FileSystemApplicationPersistenceService implements ApplicationPersistenceService {
    private final Logger LOG = LoggerFactory.getLogger(FileSystemApplicationPersistenceService.class);
    @ObjectClassDefinition
	@interface Config {
		String directory() default "application";
		long readingDelay() default 0;
		String fileExtention() default "json";
	}
	
    private File directory;
    private long readingDelay;
    private String fileExtention;
    private final List<String> files = new ArrayList<>();
    private final Map<String, Application> filesPath = new HashMap<>();
    private final Set<ApplicationAvailabilityListener> listener = new HashSet<ApplicationAvailabilityListener>();
    private Boolean active = Boolean.TRUE;
	private Thread persistenceThread;
    private static final Object lock = new Object();

	@Activate
	public void activate(Config config) {
		this.directory = new File(config.directory());
		this.readingDelay = config.readingDelay();
		this.fileExtention = config.fileExtention();
		
        persistenceThread = new Thread(() ->{
        	
        	 notifyServiceAvailable();
             while (active) {
                 try {
                     //Thread.sleep(readingDelay);
                     synchronized (lock) {
                         List<String> filesToBeProcessed = new ArrayList<>();
                         for (File applicationFile : directory.listFiles(new FilenameFilter() {
                             @Override
                             public boolean accept(File dir, String name) {
                                 return name.endsWith("." + fileExtention);
                             }
                         })) {
                             filesToBeProcessed.add(applicationFile.getAbsolutePath());
                         }
                         List<String> filesRemoved = new ArrayList<>(files);
                         filesRemoved.removeAll(filesToBeProcessed);
                         //Remove old application files
                         for (String fileRemoved : filesRemoved) {
                             notifyRemoval(fileRemoved);
                         }
                         //Process (new files or already installed) files
                         for (String toprocess : filesToBeProcessed) {
                             try {
                                 Boolean fileManaged = filesPath.containsKey(toprocess);
                                 if (!fileManaged) { //new file
                                     LOG.info("Application file {} will be loaded.", toprocess);
                                     notifyInclusion(toprocess);
                                 } else {
                                     Application applicationManaged = filesPath.get(toprocess);
                                     Application applicationInFs = FileToApplicationParser.parse(toprocess);
                                     //taken into account modified files
                                     if (!applicationManaged.getDiggest().equals(applicationInFs.getDiggest())) {
                                         LOG.info("Application file {} was already loaded but its content changed, dispatching update.", toprocess);
                                         notifyModification(toprocess);
                                         LOG.info("Application file {}, update procedure finished.", toprocess);
                                     } else {
                                         //Dont do anything, file already taken into account
                                     }
                                 }
                             } catch (Exception e) {
                                 LOG.warn("Failed to process application description file {}", toprocess, e);
                             }
                         }
                     }
                     Thread.sleep(readingDelay);
                 } catch (Exception e) {
                     LOG.error("Application persistency system failed", e);
                 }
             }
             notifyServiceUnavailable();
             LOG.error("Application persistency system is exiting");
        });
        persistenceThread.setDaemon(true);
        persistenceThread.setPriority(Thread.MIN_PRIORITY);
        persistenceThread.start();
	}

	@Deactivate
	public void deactivate() {
        if (persistenceThread != null) {
        	persistenceThread.interrupt();
        }
	}
    @Override
    public void persist(Application application) throws ApplicationPersistenceException {
        final String filename = directory + File.separator + application.getName() + "." + fileExtention;
        synchronized (lock) {
            File file = new File(filename);
            try {
                file.createNewFile();
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(application.getContent().toString().getBytes());
                fos.close();
                //filesPath.put(filename,application);
                //files.add(file.getAbsolutePath());
            } catch (IOException e) {
                LOG.error("Failed to create application file {} into the disk.", filename);
            }
        }
    }

    @Override
    public void delete(String applicationName) throws ApplicationPersistenceException {
        final String filename = directory + File.separator + applicationName + "." + fileExtention;
        synchronized (lock) {
            File file = new File(filename);
            try {
                file.delete();
            } catch (Exception e) {
                LOG.error("Failed to remove application file {} from the disk.", filename);
            }
        }
    }

    @Override
    public JsonObject fetch(String applicationName) throws ApplicationPersistenceException {
        throw new UnsupportedOperationException("Persistence to the disk is not available");
    }

    @Override
    public Collection<Application> list() {
        return Collections.unmodifiableCollection(filesPath.values());
    }

    @Override
    public void registerServiceAvailabilityListener(ApplicationAvailabilityListener listenerClient) {
        synchronized (this.listener) {
            this.listener.add(listenerClient);
        }
    }

    @Override
    public void unregisterServiceAvailabilityListener(ApplicationAvailabilityListener listenerClient) {
        synchronized (this.listener) {
            this.listener.remove(listenerClient);
        }
    }



    private void notifyInclusion(String filepath) {
        try {
            Application application = FileToApplicationParser.parse(filepath);
            LOG.info("Notifying application '{}' deployment ", filepath);
            for (ApplicationAvailabilityListener list : new HashSet<ApplicationAvailabilityListener>(listener)) {
                try {
                    synchronized (list) {
                        list.applicationFound(application.getName(), application.getContent().toString());
                    }
                } catch (Exception e) {
                    LOG.error("Failed to add application {} into the platform, is ApplicationManager running?", application.getName(), e);
                }
            }
            manageFile(filepath);
        } catch (ApplicationParseException e) {
            LOG.error("Failed to read application file", e);
        }
    }

    private void unmanageFile(String filepath) {
        files.remove(filepath);
        filesPath.remove(filepath);
    }

    private void manageFile(String filepath) {
        try {
            Application application = FileToApplicationParser.parse(filepath);
            files.add(filepath);
            filesPath.put(filepath, application);
        } catch (ApplicationParseException e) {
            files.remove(filepath);
            filesPath.remove(filepath);
            LOG.error("Error processing file.", e);
        }
    }

    private void notifyModification(String filepath) {
        LOG.info("Notifying application '{}' changed", filepath);
        try {
            Application application = FileToApplicationParser.parse(filepath);
            if (application != null) {
                for (ApplicationAvailabilityListener list : new HashSet<ApplicationAvailabilityListener>(listener)) {
                    try {
                        list.applicationChanged(application.getName(), application.getContent().toString());
                    } catch (Exception e) {
                        LOG.error("Failed to remove application from the platform", e);
                    }
                }
                manageFile(filepath);
            } else {
                LOG.warn("The application file '{}' was already notified by the system", filepath);
            }
        } catch (ApplicationParseException e) {
            e.printStackTrace();
        }
    }

    private void notifyRemoval(String filepath) {
        LOG.info("Notifying application '{}' removal", filepath);
        Application application = filesPath.get(filepath);
        unmanageFile(filepath);
        if (application != null) {
            for (ApplicationAvailabilityListener list : new HashSet<ApplicationAvailabilityListener>(listener)) {
                try {
                    list.applicationRemoved(application.getName());
                } catch (Exception e) {
                    LOG.error("Failed to remove application from the platform", e);
                }
            }
        } else {
            LOG.warn("The application file '{}' was already notified by the system", filepath);
        }
    }

    private void notifyServiceUnavailable() {
        LOG.debug("Persistence service is going offline");
        for (ApplicationAvailabilityListener list : new HashSet<ApplicationAvailabilityListener>(listener)) {
            try {
                list.serviceOffline();
            } catch (Exception e) {
                LOG.error("Persistence service is going offline", e);
            }
        }
    }

    private void notifyServiceAvailable() {
        LOG.debug("Persistence service is going online");
        for (ApplicationAvailabilityListener list : new HashSet<ApplicationAvailabilityListener>(listener)) {
            try {
                list.serviceOnline();
            } catch (Exception e) {
                LOG.error("Persistence service is going online", e);
            }
        }
    }
}
