package SampleMonitoring;

import SampleMonitoringGUI.CsvDBConnection;
import java.awt.Color;
import java.awt.event.KeyEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

public class LoginGUI extends javax.swing.JFrame {
    Connection con=null;
    ResultSet rs =null;
    PreparedStatement prep =null;
    
    
    public LoginGUI() {
        initComponents();
        con=CsvDBConnection.CsvDBConnection();
    }

    
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        usernameTf = new javax.swing.JTextField();
        passwordTf = new javax.swing.JPasswordField();
        loginBtn = new javax.swing.JButton();
        regBtn = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setBackground(java.awt.Color.white);

        jPanel2.setBackground(new java.awt.Color(255, 255, 255));
        jPanel2.setForeground(new java.awt.Color(255, 255, 255));
        jPanel2.setToolTipText("");

        jLabel3.setIcon(new javax.swing.ImageIcon(getClass().getResource("/SampleMonitoringImages/login pic_1.PNG"))); // NOI18N

        jPanel1.setBackground(new java.awt.Color(255, 255, 255));
        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Login", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Constantia", 1, 18), new java.awt.Color(8, 118, 188))); // NOI18N

        jLabel2.setText("Password:");

        jLabel1.setText("Username:");

        passwordTf.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                passwordTfActionPerformed(evt);
            }
        });
        passwordTf.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                passwordTfKeyPressed(evt);
            }
        });

        loginBtn.setBackground(new java.awt.Color(8, 118, 188));
        loginBtn.setFont(new java.awt.Font("Calibri", 0, 16)); // NOI18N
        loginBtn.setForeground(new java.awt.Color(255, 255, 255));
        loginBtn.setText("Login");
        loginBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(102, 102, 102)));
        loginBtn.setContentAreaFilled(false);
        loginBtn.setOpaque(true);
        loginBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                loginBtnMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                loginBtnMouseExited(evt);
            }
        });
        loginBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loginBtnActionPerformed(evt);
            }
        });

        regBtn.setBackground(new java.awt.Color(8, 118, 188));
        regBtn.setFont(new java.awt.Font("Calibri", 0, 16)); // NOI18N
        regBtn.setForeground(new java.awt.Color(255, 255, 255));
        regBtn.setText("Register");
        regBtn.setAutoscrolls(true);
        regBtn.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(102, 102, 102)));
        regBtn.setContentAreaFilled(false);
        regBtn.setOpaque(true);
        regBtn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                regBtnMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                regBtnMouseExited(evt);
            }
        });
        regBtn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                regBtnActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(usernameTf, javax.swing.GroupLayout.DEFAULT_SIZE, 110, Short.MAX_VALUE)
                            .addComponent(passwordTf))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGap(9, 9, 9)
                        .addComponent(regBtn, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(loginBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(usernameTf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(passwordTf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 29, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(loginBtn, javax.swing.GroupLayout.DEFAULT_SIZE, 29, Short.MAX_VALUE)
                    .addComponent(regBtn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel3)
                .addGap(18, 18, 18)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(20, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(13, 13, 13)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        setSize(new java.awt.Dimension(455, 282));
        setLocationRelativeTo(null);
    }// </editor-fold>//GEN-END:initComponents

    private void loginBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loginBtnActionPerformed
        if(con != null){
            try {
                if(con.isClosed()){
                    con=CsvDBConnection.CsvDBConnection();
                }
                else{
                    con.close();
                    con=CsvDBConnection.CsvDBConnection();
                }
            } catch (SQLException ex) {
                Logger.getLogger(LoginGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else{
            con=CsvDBConnection.CsvDBConnection();
        }
        String sql = "select * from user where user_name=? and user_password=?";
        try{
            prep=con.prepareStatement(sql);
            prep.setString(1, usernameTf.getText());
            prep.setString(2, passwordTf.getText());
            
            rs=prep.executeQuery();
            if(rs.next()){
                rs.close();
                prep.close();
                con.close();
                this.dispose();
                GUIDashboard guiD = new GUIDashboard();
                guiD.setVisible(true);
            }else{
                JOptionPane.showMessageDialog(null, "Username or Password wrong!");
            }
            
        }catch(Exception e){
            JOptionPane.showMessageDialog(null, e);
        }
    }//GEN-LAST:event_loginBtnActionPerformed

    private void passwordTfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_passwordTfActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_passwordTfActionPerformed

    private void passwordTfKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_passwordTfKeyPressed
        if(con != null){
            try {
                if(con.isClosed()){
                    con=CsvDBConnection.CsvDBConnection();
                }
                else{
                    con.close();
                    con=CsvDBConnection.CsvDBConnection();
                }
            } catch (SQLException ex) {
                Logger.getLogger(LoginGUI.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        else{
            con=CsvDBConnection.CsvDBConnection();
        }
        if(evt.getKeyCode()==KeyEvent.VK_ENTER){
            String sql = "select * from user where user_name=? and user_password=?";
            try{
                prep=con.prepareStatement(sql);
                prep.setString(1, usernameTf.getText());
                prep.setString(2, passwordTf.getText());

                rs=prep.executeQuery();
                if(rs.next()){
                    rs.close();
                    prep.close();
                    con.close();
                    this.dispose();
                    GUIDashboard guiD = new GUIDashboard();
                    guiD.setVisible(true);
                }else{
                    JOptionPane.showMessageDialog(null, "Username or Password Wrong!");
                }

            }catch(Exception e){
                JOptionPane.showMessageDialog(null, e);
            }
        }
    }//GEN-LAST:event_passwordTfKeyPressed

    private void regBtnActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_regBtnActionPerformed
        this.dispose();
        RegisterGUI reg = new RegisterGUI();
        reg.setVisible(true);
    }//GEN-LAST:event_regBtnActionPerformed

    private void regBtnMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_regBtnMouseEntered
        regBtn.setBackground(new Color(51,153,255)); 
    }//GEN-LAST:event_regBtnMouseEntered

    private void regBtnMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_regBtnMouseExited
        regBtn.setBackground(new Color(8,118,188));
    }//GEN-LAST:event_regBtnMouseExited

    private void loginBtnMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_loginBtnMouseEntered
        loginBtn.setBackground(new Color(51,153,255)); 
    }//GEN-LAST:event_loginBtnMouseEntered

    private void loginBtnMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_loginBtnMouseExited
        loginBtn.setBackground(new Color(8,118,188));
    }//GEN-LAST:event_loginBtnMouseExited

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(LoginGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(LoginGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(LoginGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(LoginGUI.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new LoginGUI().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JButton loginBtn;
    private javax.swing.JPasswordField passwordTf;
    private javax.swing.JButton regBtn;
    private javax.swing.JTextField usernameTf;
    // End of variables declaration//GEN-END:variables
}
