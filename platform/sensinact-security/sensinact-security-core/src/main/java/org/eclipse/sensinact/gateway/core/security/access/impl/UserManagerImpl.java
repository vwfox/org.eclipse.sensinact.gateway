/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.core.security.access.impl;

import java.lang.reflect.Method;
import java.security.InvalidKeyException;
import java.util.Collections;
import java.util.List;

import org.eclipse.sensinact.gateway.core.security.AbstractUserUpdater;
import org.eclipse.sensinact.gateway.core.security.SecuredAccessException;
import org.eclipse.sensinact.gateway.core.security.SecurityDataStoreService;
import org.eclipse.sensinact.gateway.core.security.User;
import org.eclipse.sensinact.gateway.core.security.UserManager;
import org.eclipse.sensinact.gateway.core.security.UserManagerFinalizer;
import org.eclipse.sensinact.gateway.core.security.UserUpdater;
import org.eclipse.sensinact.gateway.core.security.dao.DAOException;
import org.eclipse.sensinact.gateway.core.security.dao.UserDAO;
import org.eclipse.sensinact.gateway.core.security.entity.UserEntity;
import org.eclipse.sensinact.gateway.datastore.api.DataStoreException;
import org.eclipse.sensinact.gateway.util.CryptoUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.log.Logger;
import org.osgi.service.log.LoggerFactory;

/**
 * 
 * @author <a href="mailto:christophe.munilla@cea.fr">Christophe Munilla</a>
 */
@Component
public class UserManagerImpl implements UserManager {
	
	private UserDAO userDAO;
	private UserEntity anonymous;

	@Reference
	private SecurityDataStoreService dataStoreService;
	
	@Reference(service = LoggerFactory.class)
	private Logger logger;
	
	@Reference(cardinality = ReferenceCardinality.MULTIPLE,policy = ReferencePolicy.DYNAMIC)
	private volatile List<UserManagerFinalizer> userManagerFinalizers;
	
	/**
	 * 
	 * @param mediator
	 * @throws DataStoreException
	 * @throws DAOException
	 * 
	 */
	@Activate
	void start() throws SecuredAccessException {
		this.userDAO = new UserDAO(dataStoreService);

		try {
			anonymous = userDAO.find(ANONYMOUS_ID);
		} catch (DataStoreException | NullPointerException | IllegalArgumentException e) {
			logger.error("Could not load anonymous user for %s", ANONYMOUS_ID, e);
			throw new SecuredAccessException(e);
		}
	}

	@Override
	public boolean loginExists(final String login) throws SecuredAccessException, DataStoreException {
		return this.userDAO.select(Collections.singletonMap("SULOGIN", login)).size()>0;
	}

	@Override
	public boolean accountExists(String account) throws SecuredAccessException, DataStoreException {
		return this.userDAO.findFromAccount(account) != null;
	}

	@Override
	public User getUser(String login, String password) throws SecuredAccessException, DataStoreException {
		return userDAO.find(login, password);
	}
	
	@Override
	public User getUserFromPublicKey(String publicKey) throws SecuredAccessException, DataStoreException {
		if (publicKey == null) {
			return anonymous;
		}
		return userDAO.find(publicKey);
	}

	@Override
	public User getUserFromAccount(String account) throws SecuredAccessException, DataStoreException {
		if (account == null) {
			return anonymous;
		}
		return userDAO.findFromAccount(account);
	}

	@Override
	public UserUpdater createUser(String token, final String login, final String password, final String account, final String accountType) throws SecuredAccessException {
		return new AbstractUserUpdater(token, "create") {
			@Override
			public String getAccount() {
				return account;
			}
			@Override
			public String getAccountType() {
				return accountType;
			}
			@Override
			protected String doUpdate() throws SecuredAccessException {
				final String publicKey;
				String publicKeyStr = new StringBuilder().append(login).append(":").append(account
						).append(System.currentTimeMillis()).toString();
				try {
					publicKey = CryptoUtils.cryptWithMD5(publicKeyStr);
					UserEntity user = new UserEntity(login, password, account, accountType, publicKey);
					UserManagerImpl.this.userDAO.create(user);
							
					
					for (UserManagerFinalizer f : userManagerFinalizers) {
						f.userCreated(login, publicKey, account, accountType);
					}
					
					return new StringBuilder().append("Public Key : ").append(publicKey).toString();
				} catch(DAOException | DataStoreException | InvalidKeyException e) {
					throw new SecuredAccessException(e);
				}
			}
			
		};
	}

	@Override
	public UserUpdater renewUserPassword(String token, final String account, final String accountType) throws SecuredAccessException {
		return new AbstractUserUpdater(token, "renew") {
			static final String ALPHABET = ".!0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
			@Override
			public String getAccount() {
				return account;
			}
			@Override
			public String getAccountType() {
				return accountType;
			}
			@Override
			protected String doUpdate() throws SecuredAccessException {		
				try {
					User user = UserManagerImpl.this.getUserFromAccount(account);
					StringBuilder builder = new StringBuilder();					
					do {
						String millis = String.valueOf(System.currentTimeMillis());
						for(int i=millis.length()-1;i>6;i--) {
							int val = Integer.parseInt(millis.substring(i-1, i+1));
							int hval = Integer.parseInt(millis.substring(i-1, i+1),16);
							int index = -1;
							if((index = ALPHABET.indexOf(val))>-1){
								builder.append(ALPHABET.substring(index, index+1));
							} else if((index = ALPHABET.indexOf(hval))>-1){
								builder.append(ALPHABET.substring(index, index+1));
							} else if(val < millis.length()) {
								builder.append(ALPHABET.substring(val, val+1));
							}else if(hval < millis.length()) {
								builder.append(ALPHABET.substring(hval, hval+1));
							}
						}
						Thread.sleep(345);
					}while(builder.length() <= 10);
					String password = builder.toString();
					String encryptedPassword = CryptoUtils.cryptWithMD5(password);
					((UserEntity)user).setPassword(encryptedPassword);
					UserManagerImpl.this.userDAO.update((UserEntity) user);
					return new StringBuilder().append("Your new password : ").append(password).toString();
				} catch(DAOException | DataStoreException | InvalidKeyException | InterruptedException e) {
					throw new SecuredAccessException(e);
				}
			}
		};
	}

	@Override
	public void updateField(User user, String fieldName, Object oldValue, Object newValue) throws SecuredAccessException {
		try {
			Class<? extends User> clazz = user.getClass();		
			String getPrefix = "get";
			String setPrefix = "set";
			String suffix = new StringBuilder().append(fieldName.substring(0, 1
				).toUpperCase()).append(fieldName.substring(1)).toString();
			Method getMethod = clazz.getMethod(new StringBuilder().append(getPrefix).append(suffix
				).toString());
			Object current = getMethod.invoke(user);
			if((current==null && oldValue!=null)||(current!=null && !current.equals(oldValue)))
				throw new SecuredAccessException("Invalid current value");
			
			Method setMethod = clazz.getMethod(new StringBuilder().append(setPrefix).append(suffix
				).toString(), newValue.getClass());
			setMethod.invoke(user, newValue);			
		} catch (Exception e) {
			throw new SecuredAccessException(e);
		}
	}
}