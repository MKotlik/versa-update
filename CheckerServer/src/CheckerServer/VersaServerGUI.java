package CheckerServer;

import javax.swing.*;
import CheckerServer.VersaServerThread;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/* Copyright (c) 2016, Mikhail Kotlik and Sam Xu
 * Versa Checkers
 * APCS Spring Final Project
 * VersaServerGUI
 */

public class VersaServerGUI  extends JFrame{
    private VersaServer server;

    public VersaServerGUI(){
        initComponents();
        server = new VersaServer(this);
        addWindowListener(new serverWindowListener());
    }

    /**
     * This method is called by the constructor to create the form
     * WARNING: Modifying this code will affect the form editor
     * So don't touch this without editing the form file before hand
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents(){
        portField = new javax.swing.JTextField();
        listenButton = new javax.swing.JButton();
        stopButton = new javax.swing.JButton();
        connectionLabel = new javax.swing.JLabel();
        mainScrollPane = new javax.swing.JScrollPane();
        mainTextArea = new javax.swing.JTextArea();
        clientListLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("VersaServer");

        portField.setText("1216");
        portField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                portFieldActionPerformed(evt);
            }
        });

        listenButton.setText("Listen");
        listenButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                listenButtonActionPerformed(evt);
            }
        });

        stopButton.setText("Stop");
        stopButton.setEnabled(false);
        stopButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stopButtonActionPerformed(evt);
            }
        });

        connectionLabel.setText("No connection");
        connectionLabel.setFocusable(false);

        mainTextArea.setColumns(20);
        mainTextArea.setEditable(false);
        mainTextArea.setRows(5);
        mainTextArea.setFocusable(false);
        mainScrollPane.setViewportView(mainTextArea);

        clientListLabel.setText("Client List:");
        clientListLabel.setFocusable(false);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addGap(95, 95, 95)
                                                                .addComponent(portField, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(18, 18, 18)
                                                                .addComponent(listenButton)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(stopButton))
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addContainerGap()
                                                                .addComponent(clientListLabel)))
                                                .addGap(0, 103, Short.MAX_VALUE))
                                        .addGroup(layout.createSequentialGroup()
                                                .addContainerGap()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(mainScrollPane)
                                                        .addComponent(connectionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                                .addContainerGap())
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(portField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(listenButton)
                                        .addComponent(stopButton))
                                .addGap(10, 10, 10)
                                .addComponent(clientListLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(mainScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 128, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(connectionLabel))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void listenButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_listenButtonActionPerformed
        startListening();
    }//GEN-LAST:event_listenButtonActionPerformed

    private void stopButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stopButtonActionPerformed
        if (server.stopListening() == 1) {
            connectionLabel.setText("No connection");
            listenButton.setEnabled(true);
            stopButton.setEnabled(false);
        } else {
            connectionLabel.setText("Unable to stop connection");
        }
    }//GEN-LAST:event_stopButtonActionPerformed

    private void portFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_portFieldActionPerformed
        if (listenButton.isEnabled()) {
            startListening();
        }
    }//GEN-LAST:event_portFieldActionPerformed

    private void startListening() {
        int port = Integer.parseInt(portField.getText());
        if (server.startListener(port) == 1) {
            connectionLabel.setText("Listening on port " + port);
            listenButton.setEnabled(false);
            stopButton.setEnabled(true);
        } else {
            connectionLabel.setText("Unable to listen to port: " + port);
        }
    }

    public void writeClientList(String[] clientList) {
        mainTextArea.setText("");
        for (int i = 0; i < clientList.length; i++) {
            mainTextArea.append(clientList[i] + "\n");
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel clientListLabel;
    private javax.swing.JLabel connectionLabel;
    private javax.swing.JButton listenButton;
    private javax.swing.JScrollPane mainScrollPane;
    private javax.swing.JTextArea mainTextArea;
    private javax.swing.JTextField portField;
    private javax.swing.JButton stopButton;
    // End of variables declaration//GEN-END:variables

    /**
     * main method that the server will use to run
     */
    public static void main(String args[]) {
        /*
         * Set the Nimbus look and feel
         */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(VersaServerGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(VersaServerGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(VersaServerGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(VersaServerGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /*
         * Create and display the form
         */
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                new VersaServerGUI().setVisible(true);
            }
        });
    }

    //calls server to stop listening
    private class serverWindowListener extends WindowAdapter {
        @Override
        public void windowClosing(WindowEvent e) {
            server.stopListening();
        }
    }
}
