package com.cristian.servidor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.FocusManager;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;

/**
* Servidor do aplicativo de Chat.
* @author Cristian Henrique (cristianmsbr@gmail.com)
*/

public class ServidorChat extends JFrame {
	private ArrayList<PrintWriter> outputStreamsDoCliente;
	private JTextArea mensagens;
	private JButton criarServidor, sair, limparTexto;
	private JTextField porta;
	private JScrollPane barra;
	private JPanel painelSuperior, painelCentro, painelInferior;
	private JLabel creditos;

	public ServidorChat() {
		this.setTitle("Servidor");
		construirGUI();
	}

	class ManipuladorDeCliente implements Runnable {
		private BufferedReader leitor;
		private Socket sock;

		public ManipuladorDeCliente(Socket socketDoCliente) {
			try {
				sock = socketDoCliente;
				InputStreamReader inLeitor = new InputStreamReader(sock.getInputStream());
				leitor = new BufferedReader(inLeitor);
			} catch (Exception ex) { ex.printStackTrace(); }
		}

		public void run() {
			String mensagem;
			try {
				while ((mensagem = leitor.readLine()) != null) {
					mensagens.append(mensagem + "\n");
					distribuir(mensagem);
				}
			} catch (Exception ex) { ex.printStackTrace(); }
		}
	}

	private void construirGUI() {
		try {
			UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
		} catch (Exception ex) {  }
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

		criarServidor = new JButton("Criar");
		limparTexto = new JButton("Limpar texto");
		sair= new JButton("Sair");

		criarServidor.addActionListener(new CriarServidorListener());
		sair.addActionListener(new SairListener());
		limparTexto.addActionListener(new LimparListener());

		mensagens = new JTextArea(20, 38);
		mensagens.setEditable(false);
		barra = new JScrollPane(mensagens);

		painelSuperior = new JPanel();
		painelSuperior.add(porta);
		painelSuperior.add(criarServidor);
		painelSuperior.add(limparTexto);
		painelSuperior.add(sair);

		painelCentro = new JPanel();
		painelCentro.add(barra);

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
		this.setSize(450, 400);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setVisible(true);
	}

	private void distribuir(String men) {
		Iterator it = outputStreamsDoCliente.iterator();
		while (it.hasNext()) {
			try {
				PrintWriter escritor = (PrintWriter) it.next();
				escritor.println(men);
				escritor.flush();
			} catch (Exception ex) { ex.printStackTrace(); }
		}
	}

	class Servidor implements Runnable {
		public void run() {
			outputStreamsDoCliente = new ArrayList<>();
			try {
				int p = Integer.parseInt(porta.getText());
				ServerSocket sockServidor = new ServerSocket(p);

				while (true) {
					Socket socketCliente = sockServidor.accept();
					PrintWriter escritor = new PrintWriter(socketCliente.getOutputStream());
					outputStreamsDoCliente.add(escritor);
					Thread t = new Thread(new ManipuladorDeCliente(socketCliente));
					t.start();
					mensagens.append("Nova conexão\n");
				}

			} catch (Exception ex) {
				JOptionPane.showMessageDialog(null, "Erro ao criar nova conexão");
			}
		}
	}

	class CriarServidorListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			if (porta.getText().equals("")) {
				return;
			}
			porta.setEnabled(false);
			criarServidor.setEnabled(false);
			Thread t = new Thread(new Servidor());
			t.start();
		}
	}

	class SairListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			System.exit(0);
		}
	}

	class LimparListener implements ActionListener {
		public void actionPerformed(ActionEvent ev) {
			mensagens.setText("");
		}
	}

	public static void main (String[] args) {
		new ServidorChat();
	}
}