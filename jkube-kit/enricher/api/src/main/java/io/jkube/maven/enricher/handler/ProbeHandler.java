/**
 * Copyright (c) 2019 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at:
 *
 *     https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package io.jkube.maven.enricher.handler;

import io.fabric8.kubernetes.api.model.ExecAction;
import io.fabric8.kubernetes.api.model.HTTPGetAction;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.Probe;
import io.fabric8.kubernetes.api.model.TCPSocketAction;
import io.jkube.kit.common.util.CommandLine;
import io.jkube.kit.config.resource.ProbeConfig;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotBlank;


/**
 * @author roland
 * @since 07/04/16
 */
public class ProbeHandler {

    public Probe getProbe(ProbeConfig probeConfig)  {
        if (probeConfig == null) {
            return null;
        }

        Probe probe = new Probe();
        Integer initialDelaySeconds = probeConfig.getInitialDelaySeconds();
        if (initialDelaySeconds != null) {
            probe.setInitialDelaySeconds(initialDelaySeconds);
        }
        Integer timeoutSeconds = probeConfig.getTimeoutSeconds();
        if (timeoutSeconds != null) {
            probe.setTimeoutSeconds(timeoutSeconds);
        }
        Integer failureThreshold = probeConfig.getFailureThreshold();
        if(failureThreshold != null) {
            probe.setFailureThreshold(failureThreshold);
        }
        Integer successThreshold = probeConfig.getSuccessThreshold();
        if(successThreshold != null) {
            probe.setSuccessThreshold(successThreshold);
        }
        HTTPGetAction getAction = getHTTPGetAction(probeConfig.getGetUrl());
        if (getAction != null) {
            probe.setHttpGet(getAction);
            return probe;
        }
        ExecAction execAction = getExecAction(probeConfig.getExec());
        if (execAction != null) {
            probe.setExec(execAction);
            return probe;
        }
        TCPSocketAction tcpSocketAction = getTCPSocketAction(probeConfig.getGetUrl(), probeConfig.getTcpPort());
        if (tcpSocketAction != null) {
            probe.setTcpSocket(tcpSocketAction);
            return probe;
        }

        return null;
    }

    // ========================================================================================

    private HTTPGetAction getHTTPGetAction(String getUrl) {
        if (getUrl == null || !getUrl.subSequence(0,4).toString().equalsIgnoreCase("http")) {
            return null;
        }
        try {
            URL url = new URL(getUrl);
            return new HTTPGetAction(url.getHost(),
                    null /* headers */,
                    url.getPath(),
                    new IntOrString(url.getPort()),
                    url.getProtocol().toUpperCase());
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid URL " + getUrl + " given for HTTP GET readiness check");
        }
    }

    private TCPSocketAction getTCPSocketAction(String getUrl, String port) {
        if (port != null) {
            IntOrString portObj = new IntOrString(port);
            try {
                Integer portInt = Integer.parseInt(port);
                portObj.setIntVal(portInt);
            } catch (NumberFormatException e) {
                portObj.setStrVal(port);
            }
            if(getUrl==null)
                return new TCPSocketAction(getUrl, portObj);
            String validurl = getUrl.replaceFirst("(([a-zA-Z])+)://","http://");
            try{
                URL url = new URL(validurl);
                return new TCPSocketAction(url.getHost(), portObj);
            }
            catch (MalformedURLException e){
                throw new IllegalArgumentException("Invalid URL " + getUrl + " given for TCP readiness check");
            }
        }
        return null;
    }

    private ExecAction getExecAction(String execCmd) {
        if (isNotBlank(execCmd)) {
            List<String> splitCommandLine = CommandLine.translateCommandline(execCmd);
            if (!splitCommandLine.isEmpty()) {
                return new ExecAction(splitCommandLine);
            }
        }
        return null;
    }
}
