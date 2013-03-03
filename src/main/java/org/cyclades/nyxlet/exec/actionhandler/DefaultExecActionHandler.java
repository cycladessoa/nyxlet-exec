/*******************************************************************************
 * Copyright (c) 2012, THE BOARD OF TRUSTEES OF THE LELAND STANFORD JUNIOR UNIVERSITY
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *    Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *    Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *    Neither the name of the STANFORD UNIVERSITY nor the names of its contributors
 *    may be used to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *******************************************************************************/
package org.cyclades.nyxlet.exec.actionhandler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.List;
import java.util.Map;
import org.cyclades.annotations.AHandler;
import org.cyclades.engine.NyxletSession;
import org.cyclades.engine.nyxlet.templates.stroma.STROMANyxlet;
import org.cyclades.engine.nyxlet.templates.stroma.actionhandler.ActionHandler;
import org.cyclades.engine.stroma.STROMAResponseWriter;
import org.cyclades.engine.validator.IfThen;
import org.cyclades.engine.validator.ParameterExists;
import org.cyclades.engine.validator.ParameterHasIntegerValue;
import org.cyclades.engine.validator.ParameterHasValue;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;

@AHandler({"exec", "GET"})
public class DefaultExecActionHandler extends ActionHandler {

    public DefaultExecActionHandler (STROMANyxlet parentNyxlet) {
        super(parentNyxlet);
    }

    @Override
    public void handle (NyxletSession nyxletSession, Map<String, List<String>> baseParameters, 
            STROMAResponseWriter stromaResponseWriter) throws Exception {
        final String eLabel = "DefaultExecActionHandler.handle: ";
        try {
            /********************************************************************/
            /*******                  START CODE BLOCK                    *******/
            /*******                                                      *******/
            /******* YOUR CODE GOES HERE...WITHIN THESE COMMENT BLOCKS.   *******/
            /******* MODIFYING ANYTHING OUTSIDE OF THESE BLOCKS WITHIN    *******/
            /******* THIS METHOD MAY EFFECT THE STROMA COMPATIBILITY      *******/
            /******* OF THIS ACTION HANDLER.                              *******/
            /********************************************************************/
            if (parameterAsBoolean(PARAMETER_HOST, baseParameters, false)) 
                stromaResponseWriter.addResponseParameter(PARAMETER_HOST, hostName);
            int validExitCode =  (baseParameters.containsKey(PARAMETER_VALID_EXIT_CODE)) ?
                validExitCode = Integer.parseInt(baseParameters.get(PARAMETER_VALID_EXIT_CODE).get(0)) : 0;
            CommandLine cmdLine;
            DefaultExecutor executor;
            ByteArrayOutputStream stdOut = null;
            ByteArrayOutputStream stdErr = null;
            for (String command : baseParameters.get(PARAMETER_COMMAND)) {
                if (parameterAsBoolean(PARAMETER_COMMAND_IN_RESPONSE, baseParameters, false)) 
                    stromaResponseWriter.addResponseParameter(PARAMETER_COMMAND, command);
                cmdLine = CommandLine.parse(command);
                executor = new DefaultExecutor();
                executor.setWorkingDirectory(new File(baseParameters.get(PARAMETER_WORKING_DIRECTORY).get(0)));
                executor.setExitValue(validExitCode);
                if (parameterAsBoolean(PARAMETER_OUTPUT, baseParameters, false)) {
                    stdOut = new ByteArrayOutputStream();
                    stdErr = new ByteArrayOutputStream();
                    PumpStreamHandler streamHandler = new PumpStreamHandler(stdOut, stdErr);
                    executor.setStreamHandler(streamHandler);
                }
                int exitValue = executor.execute(cmdLine);
                stromaResponseWriter.addResponseParameter("exit-code", String.valueOf(exitValue));
                if (parameterAsBoolean(PARAMETER_OUTPUT, baseParameters, false)) {
                    stromaResponseWriter.addResponseParameter("std-out", stdOut.toString("UTF-8"));
                    stromaResponseWriter.addResponseParameter("std-err", stdErr.toString("UTF-8"));
                }
            }
            /********************************************************************/
            /*******                  END CODE BLOCK                      *******/
            /********************************************************************/
        } catch (Exception e) {
            getParentNyxlet().logStackTrace(e);
            handleException(nyxletSession, stromaResponseWriter, eLabel, e);
        } finally {
            stromaResponseWriter.done();
        }
    }

    /**
     * Return a valid health check status. This one simply returns true, which
     * will always flag a healthy ActionHandler...more meaningful algorithms
     * can be used.
     *
     * @return true means this is a healthy ActionHandler
     * @throws Exception
     */
    @Override
    public boolean isHealthy () throws Exception {
        return true;
    }

    @Override
    public void init () throws Exception {
        getFieldValidators()
        .add(new ParameterHasValue(PARAMETER_COMMAND))
        .add(new ParameterHasValue(PARAMETER_WORKING_DIRECTORY))
        .add(new IfThen(new ParameterExists(PARAMETER_VALID_EXIT_CODE), new ParameterHasIntegerValue(PARAMETER_VALID_EXIT_CODE)));
        hostName = java.net.InetAddress.getLocalHost().getHostName();
    }

    @Override
    public void destroy () throws Exception {
        // your destruction code here, if any
    }
    
    private String hostName;
    private static final String PARAMETER_COMMAND               = "command";
    private static final String PARAMETER_OUTPUT                = "output";
    private static final String PARAMETER_VALID_EXIT_CODE       = "valid-exit-code";
    private static final String PARAMETER_WORKING_DIRECTORY     = "working-directory";
    private static final String PARAMETER_COMMAND_IN_RESPONSE   = "command-in-response";
    private static final String PARAMETER_HOST                  = "host";

}
