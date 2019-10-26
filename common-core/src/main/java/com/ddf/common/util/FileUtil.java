package com.ddf.common.util;


import java.io.*;

public class FileUtil {

	public static void createDirs(String dir) throws IOException {
		if (StringUtil.isBlank(dir)) {
			return;
		}
		File fdir = new File(dir);
		if (!fdir.exists()) {
			fdir.mkdirs();
		}
	}

	public static String getDir(String fileName) {
		if (StringUtil.contains(fileName, "/")) {
			fileName = fileName.substring(0, fileName.lastIndexOf("/") + 1);
		} else {
			fileName = fileName.substring(0, fileName.lastIndexOf("\\") + 1);
		}
		return fileName;
	}

	public static String getFileNameOnly(String fileName) {
		if (StringUtil.contains(fileName, "/")) {
			fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
		} else {
			fileName = fileName.substring(fileName.lastIndexOf("\\") + 1);
		}
		return fileName;
	}

	public static String getFileExt(String fileName) {
		if (StringUtil.isBlank(fileName)) {
			return "";
		}
		return fileName.substring(fileName.lastIndexOf(".") + 1);
	}

	public static String getThumbnailName(String fileName) {
		if (StringUtil.isBlank(fileName)) {
			return "";
		}
		int i = fileName.lastIndexOf(".");
		return fileName.substring(0, i) + "_t." + fileName.substring(i + 1);
	}
	
	public static void copy(String oldPath, String newPath)   
	{   
		try{   
			int byteread = 0;   
			File oldfile = new File(oldPath);   
			if (oldfile.exists()) { 
				InputStream inStream = new FileInputStream(oldPath);    
                FileOutputStream fs = new FileOutputStream(newPath);   
                byte[] buffer = new byte[1444]; 
                while ((byteread = inStream.read(buffer)) != -1) {  
                	fs.write(buffer, 0, byteread);   
                }   
                inStream.close();  
                fs.close();
			}   
		}   
		catch (Exception e) {   
			e.printStackTrace();   
		}   
    }     

}
