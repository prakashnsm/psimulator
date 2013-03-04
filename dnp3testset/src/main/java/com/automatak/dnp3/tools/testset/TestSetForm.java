/**
 * Copyright 2013 Automatak, LLC
 *
 * Licensed to Automatak, LLC under one or more contributor license agreements.
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership. Automatak, LLC licenses this file to you
 * under the GNU Affero General Public License Version 3.0 (the "License");
 * you may not use this file except in compliance with the License. You may
 * obtain a copy of the License at
 *
 * http://www.gnu.org/licenses/agpl.html
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.automatak.dnp3.tools.testset;

import javax.swing.*;

import com.automatak.dnp3.*;
import com.automatak.dnp3.impl.DNP3ManagerFactory;
import com.automatak.dnp3.tools.controls.CommsTree;
import com.automatak.dnp3.tools.controls.LogTable;
import com.automatak.dnp3.tools.controls.StaticResources;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;

public class TestSetForm {

    private static void startApplication()
    {
        final DNP3Manager mgr = DNP3ManagerFactory.createDNP3ManagerWithDefaultConcurrency();
        JFrame frame = new JFrame("Automatak Protocol Simulator");
        frame.setIconImage(StaticResources.dnpIcon);
        TestSetForm form = new TestSetForm(mgr);
        frame.setContentPane(form.mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        // explicitly shutdown the dnp3 interface, detaching native threads from the JVM
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                mgr.shutdown();
            }
        });
    }

    private static void startSplash()  throws InterruptedException
    {
        SplashScreen splash = new SplashScreen(new SplashScreenListener() {
            @Override
            public void onSplashClose() {
                startApplication();
            }
        });
        splash.pack();
        splash.configure();
        splash.showSplash();
        for(int i = 0; i <= 100; ++i) //load plugins here instead of doing fake work
        {
            Thread.sleep(10);
            splash.setProcess(i);
        }
        splash.setComplete();
    }

    public static void main(String[] args) throws InterruptedException
    {
        try {
            // Set cross-platform Java L&F (also called "Metal")
            UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
        }
        catch (UnsupportedLookAndFeelException e) {
            // handle exception
        }
        catch (ClassNotFoundException e) {
            // handle exception
        }
        catch (InstantiationException e) {
            // handle exception
        }
        catch (IllegalAccessException e) {
            // handle exception
        }
        startSplash();
    }

    public TestSetForm(DNP3Manager manager)
    {
        this.manager = manager;
        this.manager.addLogSubscriber(logTable);
        this.commsTree.setManager(manager);
    }

    private JPanel mainPanel;
    private LogTable logTable;
    private JSplitPane splitPane;
    private CommsTree commsTree;
    private final DNP3Manager manager;


}
