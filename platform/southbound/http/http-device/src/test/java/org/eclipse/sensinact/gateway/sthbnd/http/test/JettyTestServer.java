/*********************************************************************
* Copyright (c) 2021 Kentyou and others
*
* This program and the accompanying materials are made
* available under the terms of the Eclipse Public License 2.0
* which is available at https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
**********************************************************************/
package org.eclipse.sensinact.gateway.sthbnd.http.test;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.sensinact.gateway.util.ReflectUtils;

import javax.servlet.AsyncContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.WriteListener;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class JettyTestServer implements Runnable {
    public class Callback {
        public final Object target;
        public final Method method;

        Callback(Object target, Method method) {
            this.target = target;
            this.method = method;
        }

        public Object invoke(Object[] parameters) throws Exception {
            this.method.setAccessible(true);
            return this.method.invoke(this.target, parameters);
        }
    }

    private Server server;
    private Map<Class<? extends Annotation>, List<Callback>> callbacks;

    public JettyTestServer(int port) throws Exception {
        this.callbacks = new HashMap<Class<? extends Annotation>, List<Callback>>();
        this.server = new Server(port);
        ServletHandler handler = new ServletHandler();
        ServletHolder holder = new ServletHolder(new JettyTestServerCallbackServlet());
        holder.setName("callbackServlet");

        handler.addServletWithMapping(holder, "/");
        this.server.setHandler(handler);
    }

    public void registerCallback(Object callback) {
        Map<Method, doGet> getMethods = ReflectUtils.getAnnotatedMethods(callback.getClass(), doGet.class);

        if (getMethods != null && getMethods.size() > 0) {
            List<Callback> callbackList = this.callbacks.get(doGet.class);

            if (callbackList == null) {
                callbackList = new ArrayList<Callback>();
                this.callbacks.put(doGet.class, callbackList);
            }
            Iterator<Method> iterator = getMethods.keySet().iterator();
            while (iterator.hasNext()) {
                callbackList.add(new Callback(callback, iterator.next()));
            }
        }
        Map<Method, doPost> postMethods = ReflectUtils.getAnnotatedMethods(callback.getClass(), doPost.class);
        if (postMethods != null && postMethods.size() > 0) {
            List<Callback> callbackList = this.callbacks.get(doPost.class);

            if (callbackList == null) {
                callbackList = new ArrayList<Callback>();
                this.callbacks.put(doPost.class, callbackList);
            }
            Iterator<Method> iterator = postMethods.keySet().iterator();
            while (iterator.hasNext()) {
                callbackList.add(new Callback(callback, iterator.next()));
            }
        }
    }

    public boolean isStarted() {
        return this.running;
    }

    public void start() throws Exception {
        this.server.start();
    }

    public void stop() throws Exception {
        this.server.stop();
    }

    public void join() throws Exception {
        this.server.join();
        Thread.sleep(2000);
    }

    @SuppressWarnings("serial")
    public class JettyTestServerCallbackServlet extends HttpServlet {
       
        public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
            if (!response.isCommitted()) {
                final AsyncContext context = request.startAsync(request, response);
                response.getOutputStream().setWriteListener(new WriteListener() {
                    @Override
                    public void onWritePossible() throws IOException {
                        doHandle(context, JettyTestServer.this.callbacks.get(doGet.class));
                    }

                    @Override
                    public void onError(Throwable t) {
                        t.printStackTrace();
                    }
                });
            }
        }

        public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
            if (!response.isCommitted()) {
                final AsyncContext context = request.startAsync(request, response);

                response.getOutputStream().setWriteListener(new WriteListener() {
                    @Override
                    public void onWritePossible() throws IOException {
                        doHandle(context, JettyTestServer.this.callbacks.get(doPost.class));
                    }

                    @Override
                    public void onError(Throwable t) {
                        t.printStackTrace();
                    }
                });
            }
        }

        /**
         * @param request
         * @param result
         * @param callbackList
         */
        private final void doHandle(AsyncContext context, List<Callback> callbackList) {
            int index = 0;
            int length = callbackList == null ? 0 : callbackList.size();
            HttpServletRequest request = (HttpServletRequest) context.getRequest();
            HttpServletResponse response = (HttpServletResponse) context.getResponse();

            for (; index < length; index++) {
                Callback callback = callbackList.get(index);
                Class<?>[] parameterTypes = callback.method.getParameterTypes();

                int parametersIndex = 0;
                int parametersLength = parameterTypes == null ? 0 : parameterTypes.length;

                Object[] parameters = new Object[parametersLength];

                for (; parametersIndex < parametersLength; parametersIndex++) {
                    Class<?> parameterClass = parameterTypes[parametersIndex];
                    if (ServletRequest.class.isAssignableFrom(parameterClass)) {
                        parameters[parametersIndex] = request;
                        continue;
                    }
                    if (ServletResponse.class.isAssignableFrom(parameterClass)) {
                        parameters[parametersIndex] = response;
                        continue;
                    }
                    if (ServletContext.class.isAssignableFrom(parameterClass)) {
                        parameters[parametersIndex] = super.getServletContext();
                        continue;
                    }
                    if (ServletConfig.class.isAssignableFrom(parameterClass)) {
                        parameters[parametersIndex] = super.getServletConfig();
                        continue;
                    }
                    parameters[parametersIndex] = super.getServletContext().getAttribute(
                    		parameterClass.getCanonicalName());
                }
                try {
                    callback.invoke(parameters);

                } catch (Exception | Error e) {
                    e.printStackTrace();
                }
            }
            context.complete();
        }
    }

    private boolean running = false;

    @Override
    public void run() {
        running = true;
        try {
            this.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        running = false;

    }
}
