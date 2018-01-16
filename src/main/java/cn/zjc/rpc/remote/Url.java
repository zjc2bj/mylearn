package cn.zjc.rpc.remote;

public class Url {
	private String host;
	private String port;

	public Url() {
	}

	public Url(String host, String port) {
		this.host = host;
		this.port = port;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

}
