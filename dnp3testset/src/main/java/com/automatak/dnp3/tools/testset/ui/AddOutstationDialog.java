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
package com.automatak.dnp3.tools.testset.ui;

import com.automatak.dnp3.LogLevel;
import com.automatak.dnp3.MasterStackConfig;
import com.automatak.dnp3.OutstationStackConfig;
import com.automatak.dnp3.PointClass;
import com.automatak.dnp3.tools.pluginapi.OutstationPluginFactory;
import com.automatak.dnp3.tools.pluginapi.StaticResources;

import javax.swing.*;
import java.awt.event.*;

public class AddOutstationDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTabbedPane tabbedPane1;
    private JCheckBox checkBoxUseConfirms;
    private UInt16Spinner spinnerLocalAddr;
    private UInt16Spinner spinnerRemoteAddr;
    private IntCountSpinner spinnerRspTimeout;
    private UInt16Spinner spinnerRetryCount;
    private UInt16Spinner spinnerMaxReceiveFragSize;
    private TimeoutSpinner spinnerAppRspTimeout;
    private LogComboBox comboxBoxLogLevel;
    private JTextArea textAreaLoggerId;
    private StaticBinaryComboBox comboBoxStaticBinary;
    private StaticAnalogComboBox comboBoxStaticAnalog;
    private StaticCounterComboBox comboBoxStaticCounter;
    private StaticAnalogOutputStatusComboBox comboBoxStaticAnalogOutputStatus;
    private EventBinaryComboBox comboBoxEventBinary;
    private EventAnalogComboBox comboBoxEventAnalog;
    private EventCounterComboBox comboBoxEventCounter;
    private JCheckBox checkBoxDisableUnsolicited;
    private JCheckBox checkBoxRequestTimeSync;
    private TimeoutSpinner spinnerUnsolTimer;
    private IntCountSpinner spinnerLinkRetry;

    private final OutstationPluginFactory factory;
    private final AddOutstationListener listener;
    private final OutstationStackConfig config;

    private final void setDefaults()
    {
        // link
        this.checkBoxUseConfirms.setSelected(config.linkConfig.useConfirms);
        this.spinnerLocalAddr.setValue(config.linkConfig.localAddr);
        this.spinnerRemoteAddr.setValue(config.linkConfig.remoteAddr);
        this.spinnerRspTimeout.setValue((int) config.linkConfig.timeoutMs);
        this.spinnerLinkRetry.setValue(config.linkConfig.numRetry);

        // app
        this.spinnerAppRspTimeout.setValue((int) config.appConfig.rspTimeoutMs);
        this.spinnerRetryCount.setValue(config.appConfig.numRetry);
        this.spinnerMaxReceiveFragSize.setValue(config.appConfig.maxFragSize);

        // outstation
        this.comboBoxStaticBinary.setSelectedItem(config.outstationConfig.staticBinaryInput);
        this.comboBoxStaticAnalog.setSelectedItem(config.outstationConfig.staticAnalogInput);
        this.comboBoxStaticCounter.setSelectedItem(config.outstationConfig.staticCounter);
        this.comboBoxStaticAnalogOutputStatus.setSelectedItem(config.outstationConfig.staticAnalogOutputStatus);
        this.comboBoxEventBinary.setSelectedItem(config.outstationConfig.eventBinaryInput);
        this.comboBoxEventAnalog.setSelectedItem(config.outstationConfig.eventAnalogInput);
        this.comboBoxEventCounter.setSelectedItem(config.outstationConfig.eventCounter);

        this.checkBoxDisableUnsolicited.setSelected(config.outstationConfig.disableUnsol);
        this.checkBoxRequestTimeSync.setSelected(config.outstationConfig.allowTimeSync);
        this.spinnerUnsolTimer.setValue((int) config.outstationConfig.unsolPackDelayMs);
    }

    private OutstationStackConfig getConfig()
    {
        OutstationStackConfig cfg = factory.getDefaultConfig();

        // link
        cfg.linkConfig.useConfirms = this.checkBoxUseConfirms.isSelected();
        cfg.linkConfig.localAddr = this.spinnerLocalAddr.getUInt16();
        cfg.linkConfig.remoteAddr = this.spinnerRemoteAddr.getUInt16();
        cfg.linkConfig.timeoutMs = this.spinnerRspTimeout.getCount();
        cfg.linkConfig.numRetry = this.spinnerLinkRetry.getCount();

        // app
        cfg.appConfig.rspTimeoutMs = this.spinnerAppRspTimeout.getTimeout();
        cfg.appConfig.numRetry = this.spinnerRetryCount.getUInt16();
        cfg.appConfig.maxFragSize = spinnerMaxReceiveFragSize.getUInt16();

        // outstation
        cfg.outstationConfig.staticBinaryInput = this.comboBoxStaticBinary.getResponse();
        cfg.outstationConfig.staticAnalogInput = this.comboBoxStaticAnalog.getResponse();
        cfg.outstationConfig.staticCounter = this.comboBoxStaticCounter.getResponse();
        cfg.outstationConfig.staticAnalogOutputStatus = this.comboBoxStaticAnalogOutputStatus.getResponse();
        cfg.outstationConfig.eventBinaryInput = this.comboBoxEventBinary.getResponse();
        cfg.outstationConfig.eventAnalogInput = this.comboBoxEventAnalog.getResponse();
        cfg.outstationConfig.eventCounter = this.comboBoxEventCounter.getResponse();

        cfg.outstationConfig.disableUnsol = this.checkBoxDisableUnsolicited.isSelected();
        cfg.outstationConfig.allowTimeSync = this.checkBoxRequestTimeSync.isSelected();
        cfg.outstationConfig.unsolPackDelayMs = this.spinnerUnsolTimer.getTimeout();

        return cfg;
    }

    public AddOutstationDialog(OutstationPluginFactory factory, AddOutstationListener listener) {

        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        this.factory = factory;
        this.listener = listener;
        this.config = factory.getDefaultConfig();

        this.setTitle("Add Outstation using plugin: " + factory.getPluginName());
        this.setIconImage(StaticResources.dnpIcon);
        this.textAreaLoggerId.setText(factory.getPluginName());

        this.setDefaults();

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

// call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

// call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        OutstationStackConfig cfg = getConfig();
        LogLevel level = comboxBoxLogLevel.getLogLevel();
        String id = textAreaLoggerId.getText();
        this.listener.onAdd(id, level, cfg);
        dispose();
    }

    private void onCancel() {
// add your code here if necessary
        dispose();
    }
}
