package com.sucre;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.print.attribute.HashAttributeSet;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;

import com.sucre.mainUtil.MyUtil;
import com.sucre.myNet.Nets;
import com.sucre.myNet.OkHttp;

/**
 * 建立一个winform以方便操作及显示最新反馈.
 * 
 * @author sucre
 * @version 0.01
 */
public class MainFrom extends JFrame implements ActionListener {

	// 创建一个button,并设置标题
	JButton send = new JButton("发送");
	// 创建一个textbox

	JTextField hostName = new JTextField();
	JTextField tNum = new JTextField();
	JTextArea postData = new JTextArea();
	JTextArea feedBack = new JTextArea();
	JTextArea autoSend = new JTextArea();//定时发送毫秒数

	MainFrom(String title) {
		// 定义x,y,w,h,位置及大小
		int x = 750, y = 80, w = 100, h = 20;
		JFrame jf = new JFrame(title);
		// 设置关闭按钮的操作.
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// 设置窗体在屏幕的位置
		jf.setLocation(300, 120);
		// 设置窗体大小.
		jf.setSize(880, 650);

		// JButton addVid= new JButton("加入vid");
		// 设置按钮位置大小.
		send.setBounds(727, 82, 100, 20);

		// 设置textbox位置
		// feedBack.setBounds(10, 10, 100, 100);
		hostName.setBounds(727, 24, 123, 20);
		tNum.setBounds(727, 52, 43, 20);
		autoSend.setBounds(727, 112, 43, 20);

		// 为textbox设置内容和属性
		hostName.setText("host");
		tNum.setText("443");
		jf.getContentPane().setLayout(null);
		// feedBack.
		// 把按钮增加到窗体
		jf.getContentPane().add(send);
		// scroll.setPreferredSize(new Dimension(450, 110));

		// 加载textbox到窗体
		// jf.add(feedBack);
		jf.getContentPane().add(hostName);
		jf.getContentPane().add(tNum);
		jf.getContentPane().add(autoSend);

		JScrollPane scroll = new JScrollPane();
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scroll.setBounds(10, 328, 694, 274);
		jf.getContentPane().add(scroll);
		feedBack.setText("目前功能：randme=随机6位数\r\ngettime=当前时间\r\nRR(n)=生成n位数字的随机数\r\nNN(n)=生成n位数字和字母混合随机数");

		scroll.setViewportView(feedBack);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setBounds(10, 10, 694, 308);
		jf.getContentPane().add(scrollPane);

		scrollPane.setViewportView(postData);
		// jf.add(packet);

		// feedBack.setBounds(10, 10, 400, 400);
		// scroll.add(feedBack);
		// scroll.updateUI();
		// 添加控件之后要重画窗体.
		jf.repaint();
		jf.setVisible(true);
		// 为按钮增加事件.
		send.addActionListener(this);

	}

	// 接收按钮事件.
	@Override
	public void actionPerformed(ActionEvent e) {
		// 取具体按钮事件
		Object b = e.getSource();
		if (b.equals(send)) {
			Thread thread=new Thread(){
				public void run(){
					String auto=autoSend.getText().toString();
					do {
						//byte[] data = getPakect(postData.getText());
						//Nets net = new Nets();
						//int port = Integer.parseInt(tNum.getText().toString());
						String ret = "";
						/*if (port == 443) {
							//ret = net.goPost(hostName.getText().toString(), port, data);

						} else {

							//ret = net.GoHttp(hostName.getText().toString(), port, data);
						}*/
						ret=getPakect(postData.getText());
						feedBack.setText(ret);
						if(!auto.equals("")){
							MyUtil.sleeps(Integer.parseInt(auto));
						}
					}while ( !auto.equals(""));

				}
			};

			thread.start();
			
		}
	}

	private String getPakect(String data) {
		data = data.replaceAll("randme", MyUtil.getRand(999999, 111111));
		data = data.replaceAll("gettime", MyUtil.getTime());
		// 生成随机数字
		int begin, ends;
		while ((begin = data.indexOf("RR(")) != -1) {
			ends = data.indexOf(")", begin);
			String t = data.substring(begin + 3, ends);
			data = data.replaceAll("RR\\(" + t + "\\)", MyUtil.makeNumber(Integer.parseInt(t)));

		}
		// 生成随机字符
		// int begin,ends;
		while ((begin = data.indexOf("NN(")) != -1) {
			ends = data.indexOf(")", begin);
			String t = data.substring(begin + 3, ends);
			data = data.replaceAll("NN\\(" + t + "\\)", MyUtil.makeNonce(Integer.parseInt(t)));

		}
		String url;
		OkHttp okHttp=new OkHttp();

		//把header 全部取出来，进一步再装入map里
		String headerData=MyUtil.midWrod("HTTP/1.1\n","\n\n",data);
		HashMap<String,String> header=getHeader(headerData);
		// POST/GET 数据,分别调用okhttp
		if (data.startsWith("POST")) {
			// 取出post 的from
			String pd = "";
			//post Data
			pd = MyUtil.midWrod("\n\n", "\n\n", data);
            //取url
			url=MyUtil.midWrod("POST "," HTTP",data);
			//准备装body post Data
			//先检查是json 还是key value形式
			if(pd.startsWith("{")){
				return okHttp.goPost(url,header,pd);
			}else{
				HashMap<String,String> body=getBody(pd);
				return okHttp.goPost(url,header,body);
			}

		}else{
			//取url
			url=MyUtil.midWrod("GET "," HTTP",data);
			return okHttp.goGet(url,header);
		}

	}

	/**
	 * 把文本里的header转为map 返回
	 * @param header
	 * @return
	 */
	private HashMap<String,String> getHeader(String header){
		String[] rowHeader=header.split("\n");
		HashMap<String,String> result=new HashMap<>();
		for(String key:rowHeader){
			String[] kvHeader=key.split(": ");
			result.put(kvHeader[0],kvHeader[1]);
		}
		return result;
	}

	/**
	 * 把文本里的body 转为map
	 * @param body
	 * @return
	 */
	private HashMap<String,String> getBody(String body){
		String[] rowBody=body.split("&");
		HashMap<String,String> result=new HashMap<>();
		for(String key:rowBody){
			String[] kvBody=key.split("=");
			if (kvBody.length>1){
				result.put(kvBody[0],kvBody[1]);
			}else{
				result.put(kvBody[0],"");
			}

		}
		return result;
	}
}
