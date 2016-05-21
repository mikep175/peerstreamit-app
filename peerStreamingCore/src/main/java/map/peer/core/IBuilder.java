package map.peer.core;

public interface IBuilder {

	void stop();
	
	void controlLoop(String fileUrl, String nsi, DBHelper dbHelper);
}
