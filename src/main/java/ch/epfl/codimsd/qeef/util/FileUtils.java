package ch.epfl.codimsd.qeef.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileUtils {
	
    /**
     * Create a directory structure if it doesn't exist.
     * @param fullPathName Full path name to be created
     */
    public static void createDirectoryStructure (String fullPathName) {
        File pathName = new File(fullPathName);

        if (! pathName.isDirectory()) {
            pathName.mkdirs();
        }
    }
	
    /**
     * 
     * @param file
     * @return
     * @throws IOException
     */
    public static byte[] readFile(File file) throws IOException {
		byte[] result = new byte[(int)file.length()];
		FileInputStream in = new FileInputStream(file);
		in.read(result);
		in.close();
		return result;
	}
	
    /**
     * 
     * @param file
     * @param bytes
     * @throws IOException
     */
    public static void writeFile(File file, byte[] bytes) throws IOException {
		FileOutputStream out = new FileOutputStream(file);
		out.write(bytes);
		out.close();
	}
    
	/**
	 * 
	 * @param file
	 * @param temp
	 * @param bytes
	 * @throws IOException
	 */
    public static void writeFile(final File file, final File temp, final byte[] bytes) throws IOException {
		writeFile(temp, bytes);
		moveFile(temp, file);
	}
	
    
	public static void moveFile(File temp, String file) {
		moveFile(temp, new File(file));
	}
    
	/**
	 * 
	 * @param temp
	 * @param file
	 */
	public static void moveFile(File temp, File file) {
		if (file.isFile() && file.exists())
			file.delete();
		
		if (file.isDirectory()) {
			file = new File(file.getAbsolutePath() + System.getProperty("file.separator") + temp.getName());
		}
		
		temp.renameTo(file);
	}
	
    /**
     * 
     * @param file
     */
	public static void deleteFile(File file) {
		if (file.exists())
			file.delete();
	}
	
	/**
	 * Gets a list of files with a given extension recursively in subdirectories. 
	 * Directories are not included.
	 * @param file Initial search directory 
	 * @param regex Regular expression used to find files.
	 * @return List of files with a given extension.
	 */
	public static List<File> getFiles(String file, String regex, boolean recurseSubdirectories) {
		if (file == null || file.length() == 0)
			file = ".";
		return getFiles(new File(file), regex, recurseSubdirectories);
	}
	
	/**
	 * Gets a list of files with a given extension recursively in subdirectories. 
	 * Directories are not included.
	 * @param file Initial search directory 
	 * @param regex Regular expression used to find files.
	 * @return List of files with a given extension.
	 */
	public static List<File> getFiles(File file, String regex, boolean recurseSubdirectories) {
		List<File> fileList = new ArrayList<File>();
		getFiles(fileList, file, regex, recurseSubdirectories);
		return fileList;
	}
	
	/**
	 * Gets a list of files with a given extension recursively in subdirectories. 
	 * @param fileList List of files with a given extension.
	 * @param file Initial search directory 
	 * @param regex Regular expression used to find files.
	 * @param recurseSubdirectories if files search is recursive.
	 */
	private static void getFiles(List<File> fileList, File file, String regex, boolean recurseSubdirectories) {
		if (file.isFile()) {
			fileList.add(file);
		} else {
			File[] files = file.listFiles();
			for (File item : files) {
				if (item.isDirectory() && recurseSubdirectories)
					getFiles(fileList, item, regex, recurseSubdirectories);
				else if (regex == null || item.getName().matches(regex))
					fileList.add(item);
			}
		}
	}
    
}