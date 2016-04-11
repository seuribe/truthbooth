package de.peb.truthbooth;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UserInformation {

	private static final String BASE_REC_DIR = "recordings/";
	
	public static enum Privacy {
		Public, Private, Research;
   	}
/*	
	public static enum Language {
		None("common"), English("en"), Deutsch("de");
		
		private String code;
		private Language(String code) {
			this.code = code;
		}
		
		public String getCode() { return code; }
	}
*/
	public static enum RecordType {
		Video, Audio;
	}
	
//    private Language language = Language.None;
	private String language;
    private Privacy privacy;
    private RecordType type;

    private String timestamp;

    private File tempFile;
    
    public boolean recordVideo() {
    	return type == RecordType.Video;
    }
    
    public void deleteTemporaryFile() {
    	try {
			getTemporaryFile().delete();
        	getTemporaryFile().deleteOnExit();
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    public File getTemporaryFile() {
    	if (tempFile == null) {
			tempFile = new File(BASE_REC_DIR + language + "_" + timestamp + "_" + type + "_" + privacy + "_temp" + TruthBooth.FILE_EXTENSION);
    	}
    	return tempFile;
    }
	public File getFinalFile() {
		return new File(BASE_REC_DIR + language + "/" + privacy + "/" + language + "_" + timestamp + "_" + type + "_" + privacy + TruthBooth.FILE_EXTENSION);
	}

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setRecordType(RecordType type) {
    	this.type = type;
    }

    public void setArchivePrivacy(Privacy privacy) {
    	this.privacy = privacy;
    }

    public Privacy getArchivePrivacy() {
    	return this.privacy;
    }
    
    public File getDumpFilename() {
		return new File(BASE_REC_DIR + language + "/" + timestamp + ".dump");
    }
    

    public UserInformation() {
        this.timestamp = (new SimpleDateFormat("yyyyMMdd-HHmmss")).format(new Date());
    }

	public void renameToFinal() {
		File tempFile = getTemporaryFile();
		File finalFile = getFinalFile();
		finalFile.getParentFile().mkdirs();
		tempFile.renameTo(finalFile);
	}

	public String getLanguage() {
		return language;
	}

}