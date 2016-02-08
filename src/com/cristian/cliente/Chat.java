package com.cristian.cliente;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.IOException;
import java.net.URI;
import java.net.Socket;
import javax.swing.JFrame;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;
import javax.swing.FocusManager;

/**
* Cliente do aplicativo de Chat.
* @author Cristian Henrique (cristianmsbr@gmail.com)
*/

public class Chat extends JFrame {
	private JTextField usuario, porta, escreverMensagem;
	private JTextArea mensagens;
	private JButton conectar, enviar, sair;
	private JScrollPane barra;
	private JPanel painelSuperior, painelCentro, painelInferior;
	private JLabel creditos;
	private BufferedReader leitor;
	private PrintWriter escritor;
	private Socket sock;

	public Chat() {
		this.setTitle("Chat");
		construirGUI();
	}

	private void construirGUI() {
		try {
			UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (Exception ex) {  }
		usuario = new JTextField(15) {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);

				if (getText().isEmpty() && ! (FocusManager.getCurrentKeyboardFocusManager().getFocusOwner() == this)) {
					Graphics2D g2 = (Graphics2D) g.create();
					g2.setBackground(Color.GRAY);
					g2.setFont(getFont().deriveFont(Font.PLAIN));
					g2.drawString("Usuário", 5, 18);
					g2.dispose();
				}
			}};
		porta = new JTextField(8) {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);

				if (getText().isEmpty() && ! (FocusManager.getCurrentKeyboardFocusManager().getFocusOwner() == this)) {
					Graphics2D g2 = (Graphics2D) g.create();
					g2.setBackground(Color.GRAY);
					g2.setFont(getFont().deriveFont(Font.PLAIN));
					g2.drawString("Porta", 5, 18);
					g2.dispose();
				}
			}};

		conectar = new JButton("Conectar");
		enviar = new JButton("Enviar");
		sair = new JButton("Sair");

		conectar.addActionListener(new ConectarListener());
		sair.addActionListener(new SairListener());
		enviar.addActionListener(new EnviarListener());

		mensagens = new JTextArea(20, 38);
		mensagens.setEditable(false);
		barra = new JScrollPane(mensagens);

		escreverMensagem = new JTextField(32);

		painelSuperior = new JPanel();
		painelSuperior.add(usuario);
		painelSuperior.add(porta);
		painelSuperior.add(conectar);
		painelSuperior.add(sair);

		painelCentro = new JPanel();
		painelCentro.add(barra);
		painelCentro.add(escreverMensagem);
		painelCentro.add(enviar);

		creditos = new JLabel("2016 - Cristian Henrique");
		creditos.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent ev) {
				try {
					Desktop.getDesktop().browse(URI.create("https://www.github.com/cristian-henrique/"));
				} catch (Exception ex) {  }
			}
		});
		creditos.setCursor(new Cursor(Cursor.HAND_CURSOR));
		painelInferior = new JPanel();
		painelInferior.add(creditos);

		this.getContentPane().add(BorderLayout.NORTH, painelSuperior);
		this.getContentPane().add(BorderLayout.CENTER, painelCentro);
		this.getContentPane().add(BorderLayout.SOUTH, painelInferior);
		this.setResizable(false);
		this.setSize(450, 425);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
	}

	private void configurarConexao() {
		try {
			int p = Integer.parseInt(porta.getText());
			sock = new Socket("127.0.0.1", p);
			InputStreamReader streamReader = new InputStreamReader(sock.getInputStream());
			leitor = new BufferedReader(streamReader);
			escritor = new PrintWriter(sock.getOutputStream());
			mensagens.append("Nova conexão.\n");

			Thread t = new Thread(new LeitorDeEntrada());
			t.start();

			usuario.setEnabled(false);
			porta.setEnabled(false);
			conectar.setEnabled(false);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, "Erro ao configurar a conexão");
		}
	}

	class ConectarListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			if (usuario.getText().equals("") || porta.getText().equals("")) {
				return;
			}

			configurarConexao();
		}
	}

	class SairListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			System.exit(0);
		}
	}

	class EnviarListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			try {
				escritor.println(usuario.getText() + ": " + escreverMensagem.getText());
				escritor.flush();
			} catch (Exception ex) { ex.printStackTrace(); }
			escreverMensagem.setText("");
			escreverMensagem.requestFocus();
		}
	}

	class LeitorDeEntrada implements Runnable {
		public void run() {
			String msg;
			try {
				while ((msg = leitor.readLine()) != null) {
					mensagens.append(msg + "\n");
				}
			} catch (Exception ex) { ex.printStackTrace(); }
		}
	}

	public static void main (String[] args) {
		new Chat();
	}
}