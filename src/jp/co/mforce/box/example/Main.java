package jp.co.mforce.box.example;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.box.sdk.BoxAPIConnection;
import com.box.sdk.BoxAPIRequest;
import com.box.sdk.BoxAPIResponse;
import com.box.sdk.BoxFile;
import com.box.sdk.BoxFolder;
import com.box.sdk.BoxItem;
import com.box.sdk.BoxSearch;
import com.box.sdk.BoxSearchParameters;
import com.box.sdk.BoxUser;
import com.box.sdk.PartialCollection;

public final class Main {
    private static final String DEVELOPER_TOKEN = "VVhabIPwmsvzqwhTxnMphcc9BEAkLKYl";
    private static final int MAX_DEPTH = 1;
    private static final String ROOT_URL = "https://account.box.com/api/oauth2/authorize";
    private static final String PROP_FILE_PATH = "test.properties";


    private Main() { }

    public static void main(String[] args) {
        // Turn off logging to prevent polluting the output.
        Logger.getLogger("com.box.sdk").setLevel(Level.OFF);

        //プロパティファイル読み込み
        try{
        	Properties prop = new Properties();
        	InputStream inputStream = new FileInputStream(PROP_FILE_PATH);
            prop.load(inputStream);
            inputStream.close();

            URL url = new URL(ROOT_URL + "?response_type=code&client_id=" + prop.getProperty("client_id") + "&state=XXX");
            BoxAPIRequest req = new BoxAPIRequest(url, "GET");
            BoxAPIResponse res = req.send();
            BufferedReader reader = new BufferedReader(new InputStreamReader(res.getBody(),Charset.forName("UTF-8")));
            System.out.println(reader);
            String line = null;
			while((line = reader.readLine()) != null){
				System.out.println(line);
			}

        BoxAPIConnection api = new BoxAPIConnection(DEVELOPER_TOKEN);

        BoxUser.Info userInfo = BoxUser.getCurrentUser(api).getInfo();
        System.out.format("Welcome, %s !\n\n", userInfo.getName());

        BoxSearch boxSearch = new BoxSearch(api);
        BoxSearchParameters bsp_in = new BoxSearchParameters("dir1-1");
        BoxSearchParameters bsp_out = new BoxSearchParameters("dir1-2");
        bsp_in.setType("folder");
        bsp_out.setType("folder");
        PartialCollection<BoxItem.Info> searchResult1 = boxSearch.searchRange(0, 1, bsp_in);
        PartialCollection<BoxItem.Info> searchResult2 = boxSearch.searchRange(0, 1, bsp_out);
        BoxFolder inFolder = null;
        BoxFolder outFolder = null;
        for(BoxItem.Info itemInfo : searchResult1){
        	inFolder = (BoxFolder) itemInfo.getResource();
        }
        for(BoxItem.Info itemInfo : searchResult2){
        	outFolder = (BoxFolder) itemInfo.getResource();
        }
        if(inFolder != null || outFolder != null){
        	for(BoxItem.Info item : inFolder.getChildren()){
    			if(item instanceof BoxFile.Info){
    				try {
    					OutputStream out = new FileOutputStream(item.getName());
    					BoxFile f = (BoxFile) item.getResource();
    					f.download(out);
    					f.move(outFolder);
    					System.out.println("SUCCESS!!");
    				} catch(Exception e) {
    					e.printStackTrace();
    				}
    			}
    		}
        }
        }catch (Exception e) {
        	e.printStackTrace();
        }
    }

    private static void listFolder(BoxFolder folder, int depth) {
        for (BoxItem.Info itemInfo : folder) {
            String indent = "";
            for (int i = 0; i < depth; i++) {
                indent += "    ";
            }

            System.out.println(indent + itemInfo.getName());
            if (itemInfo instanceof BoxFolder.Info) {
                BoxFolder childFolder = (BoxFolder) itemInfo.getResource();
                if (depth < MAX_DEPTH) {
                    listFolder(childFolder, depth + 1);
                }
            }
        }
    }
}
