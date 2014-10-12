package com.loic.common.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.xeustechnologies.jtar.TarEntry;
import org.xeustechnologies.jtar.TarInputStream;
import org.xeustechnologies.jtar.TarOutputStream;

import android.util.Log;

public class FileUtils 
{
    public final static String FILE_EXTENSION_SEPARATOR = ".";
    private static final String TAG = FileUtils.class.getSimpleName();

    /**
     * read file
     * 
     * @param filePath
     * @param charsetName The name of a supported {@link java.nio.charset.Charset </code>charset<code>}
     * @return if file not exist, return null, else return content of file
     * @throws RuntimeException if an error occurs while operator BufferedReader
     */
    public static StringBuilder readFile(String filePath, String charsetName) 
    {
        File file = new File(filePath);
        
        if (file == null || !file.isFile()) {
            return null;
        }
        
        StringBuilder fileContent = new StringBuilder();

        BufferedReader reader = null;
        try {
            InputStreamReader is = new InputStreamReader(new FileInputStream(file), charsetName);
            reader = new BufferedReader(is);
            String line = null;
            while ((line = reader.readLine()) != null) {
                if (!fileContent.toString().equals("")) {
                    fileContent.append("\r\n");
                }
                fileContent.append(line);
            }
            reader.close();
            return fileContent;
        } catch (IOException e) {
            throw new RuntimeException("IOException occurred. ", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    throw new RuntimeException("IOException occurred. ", e);
                }
            }
        }
    }

    /**
     * write file
     * 
     * @param filePath
     * @param content
     * @param append is append, if true, write to the end of file, else clear content of file and write into it
     * @return return false if content is empty, true otherwise
     * @throws RuntimeException if an error occurs while operator FileWriter
     */
    public static boolean writeFile(String filePath, String content, boolean append) 
    {
        if (content.isEmpty())
            return false;

        FileWriter fileWriter = null;
        try 
        {
            makeDirs(filePath);
            fileWriter = new FileWriter(filePath, append);
            fileWriter.write(content);
            fileWriter.close();
            return true;
        } catch (IOException e) {
            throw new RuntimeException("IOException occurred. ", e);
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    throw new RuntimeException("IOException occurred. ", e);
                }
            }
        }
    }

    /**
     * write file
     * 
     * @param filePath
     * @param contentList
     * @param append is append, if true, write to the end of file, else clear content of file and write into it
     * @return return false if contentList is empty, true otherwise
     * @throws RuntimeException if an error occurs while operator FileWriter
     */
    public static boolean writeFile(String filePath, List<String> contentList, boolean append) {
        if (contentList == null || contentList.size() == 0)
            return false;

        FileWriter fileWriter = null;
        try {
            makeDirs(filePath);
            fileWriter = new FileWriter(filePath, append);
            int i = 0;
            for (String line : contentList) {
                if (i++ > 0) {
                    fileWriter.write("\r\n");
                }
                fileWriter.write(line);
            }
            fileWriter.close();
            return true;
        } catch (IOException e) {
            throw new RuntimeException("IOException occurred. ", e);
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    throw new RuntimeException("IOException occurred. ", e);
                }
            }
        }
    }

    /**
     * write file, the string will be written to the begin of the file
     * 
     * @param filePath
     * @param content
     * @return
     */
    public static boolean writeFile(String filePath, String content) 
    {
        return writeFile(filePath, content, false);
    }

    /**
     * write file, the string list will be written to the begin of the file
     * 
     * @param filePath
     * @param contentList
     * @return
     */
    public static boolean writeFile(String filePath, List<String> contentList) 
    {
        return writeFile(filePath, contentList, false);
    }

    /**
     * write file, the bytes will be written to the begin of the file
     * 
     * @param filePath
     * @param stream
     * @return
     * @see {@link #writeFile(String, InputStream, boolean)}
     */
    public static boolean writeFile(String filePath, InputStream stream) 
    {
        return writeFile(filePath, stream, false);
    }

    /**
     * write file
     * 
     * @param file the file to be opened for writing.
     * @param stream the input stream
     * @param append if <code>true</code>, then bytes will be written to the end of the file rather than the beginning
     * @return return true
     * @throws RuntimeException if an error occurs while operator FileOutputStream
     */
    public static boolean writeFile(String filePath, InputStream stream, boolean append) 
    {
        return writeFile(filePath != null ? new File(filePath) : null, stream, append);
    }

    /**
     * write file, the bytes will be written to the begin of the file
     * 
     * @param file
     * @param stream
     * @return
     * @see {@link #writeFile(File, InputStream, boolean)}
     */
    public static boolean writeFile(File file, InputStream stream) 
    {
        return writeFile(file, stream, false);
    }

    /**
     * write file
     * 
     * @param file the file to be opened for writing.
     * @param stream the input stream
     * @param append if <code>true</code>, then bytes will be written to the end of the file rather than the beginning
     * @return return true
     * @throws RuntimeException if an error occurs while operator FileOutputStream
     */
    public static boolean writeFile(File file, InputStream stream, boolean append) 
    {
        OutputStream o = null;
        try {
            makeDirs(file.getAbsolutePath());
            o = new FileOutputStream(file, append);
            byte data[] = new byte[1024];
            int length = -1;
            while ((length = stream.read(data)) != -1) {
                o.write(data, 0, length);
            }
            o.flush();
            return true;
        } catch (FileNotFoundException e) {
            throw new RuntimeException("FileNotFoundException occurred. ", e);
        } catch (IOException e) {
            throw new RuntimeException("IOException occurred. ", e);
        } finally {
            if (o != null) {
                try {
                    o.close();
                    stream.close();
                } catch (IOException e) {
                    throw new RuntimeException("IOException occurred. ", e);
                }
            }
        }
    }

    /**
     * copy file
     * 
     * @param sourceFilePath
     * @param destFilePath
     * @return
     * @throws RuntimeException if an error occurs while operator FileOutputStream
     */
    public static boolean copyFile(String sourceFilePath, String destFilePath) 
    {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(sourceFilePath);
        } catch (FileNotFoundException e) {
            throw new RuntimeException("FileNotFoundException occurred. ", e);
        }
        return writeFile(destFilePath, inputStream);
    }

    /**
     * read file to string list, a element of list is a line
     * 
     * @param filePath
     * @param charsetName The name of a supported {@link java.nio.charset.Charset </code>charset<code>}
     * @return if file not exist, return null, else return content of file
     * @throws RuntimeException if an error occurs while operator BufferedReader
     */
    public static List<String> readFileToList(String filePath, String charsetName) {
        File file = new File(filePath);
        List<String> fileContent = new ArrayList<String>();
        if (file == null || !file.isFile()) {
            return null;
        }

        BufferedReader reader = null;
        try {
            InputStreamReader is = new InputStreamReader(new FileInputStream(file), charsetName);
            reader = new BufferedReader(is);
            String line = null;
            while ((line = reader.readLine()) != null) {
                fileContent.add(line);
            }
            reader.close();
            return fileContent;
        } catch (IOException e) {
            throw new RuntimeException("IOException occurred. ", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    throw new RuntimeException("IOException occurred. ", e);
                }
            }
        }
    }

    /**
     * get file name from path, not include suffix
     * 
     * <pre>
     *      getFileNameWithoutExtension(null)               =   null
     *      getFileNameWithoutExtension("")                 =   ""
     *      getFileNameWithoutExtension("   ")              =   "   "
     *      getFileNameWithoutExtension("abc")              =   "abc"
     *      getFileNameWithoutExtension("a.mp3")            =   "a"
     *      getFileNameWithoutExtension("a.b.rmvb")         =   "a.b"
     *      getFileNameWithoutExtension("c:\\")              =   ""
     *      getFileNameWithoutExtension("c:\\a")             =   "a"
     *      getFileNameWithoutExtension("c:\\a.b")           =   "a"
     *      getFileNameWithoutExtension("c:a.txt\\a")        =   "a"
     *      getFileNameWithoutExtension("/home/admin")      =   "admin"
     *      getFileNameWithoutExtension("/home/admin/a.txt/b.mp3")  =   "b"
     * </pre>
     * 
     * @param filePath
     * @return file name from path, not include suffix
     * @see
     */
    public static String getFileNameWithoutExtension(String filePath) {
        if (filePath.isEmpty())
            return filePath;

        int extenPosi = filePath.lastIndexOf(FILE_EXTENSION_SEPARATOR);
        int filePosi = filePath.lastIndexOf(File.separator);
        if (filePosi == -1) {
            return (extenPosi == -1 ? filePath : filePath.substring(0, extenPosi));
        }
        if (extenPosi == -1) {
            return filePath.substring(filePosi + 1);
        }
        return (filePosi < extenPosi ? filePath.substring(filePosi + 1, extenPosi) : filePath.substring(filePosi + 1));
    }

    /**
     * get file name from path, include suffix
     * 
     * <pre>
     *      getFileName(null)               =   null
     *      getFileName("")                 =   ""
     *      getFileName("   ")              =   "   "
     *      getFileName("a.mp3")            =   "a.mp3"
     *      getFileName("a.b.rmvb")         =   "a.b.rmvb"
     *      getFileName("abc")              =   "abc"
     *      getFileName("c:\\")              =   ""
     *      getFileName("c:\\a")             =   "a"
     *      getFileName("c:\\a.b")           =   "a.b"
     *      getFileName("c:a.txt\\a")        =   "a"
     *      getFileName("/home/admin")      =   "admin"
     *      getFileName("/home/admin/a.txt/b.mp3")  =   "b.mp3"
     * </pre>
     * 
     * @param filePath
     * @return file name from path, include suffix
     */
    public static String getFileName(String filePath) 
    {
        if (filePath.isEmpty()) {
            return filePath;
        }

        int filePosi = filePath.lastIndexOf(File.separator);
        return (filePosi == -1) ? filePath : filePath.substring(filePosi + 1);
    }

    /**
     * get folder name from path
     * 
     * <pre>
     *      getFolderName(null)               =   null
     *      getFolderName("")                 =   ""
     *      getFolderName("   ")              =   ""
     *      getFolderName("a.mp3")            =   ""
     *      getFolderName("a.b.rmvb")         =   ""
     *      getFolderName("abc")              =   ""
     *      getFolderName("c:\\")              =   "c:"
     *      getFolderName("c:\\a")             =   "c:"
     *      getFolderName("c:\\a.b")           =   "c:"
     *      getFolderName("c:a.txt\\a")        =   "c:a.txt"
     *      getFolderName("c:a\\b\\c\\d.txt")    =   "c:a\\b\\c"
     *      getFolderName("/home/admin")      =   "/home"
     *      getFolderName("/home/admin/a.txt/b.mp3")  =   "/home/admin/a.txt"
     * </pre>
     * 
     * @param filePath
     * @return
     */
    public static String getFolderName(String filePath) 
    {
        if (filePath.isEmpty())
            return filePath;

        int filePosi = filePath.lastIndexOf(File.separator);
        return (filePosi == -1) ? "" : filePath.substring(0, filePosi);
    }

    /**
     * Creates the directory named by the trailing filename of this file, including the complete directory path required
     * to create this directory. <br/>
     * <br/>
     * <ul>
     * <strong>Attentions:</strong>
     * <li>makeDirs("C:\\Users\\Trinea") can only create users folder</li>
     * <li>makeFolder("C:\\Users\\Trinea\\") can create Trinea folder</li>
     * </ul>
     * 
     * @param filePath
     * @return true if the necessary directories have been created or the target directory already exists, false one of
     *         the directories can not be created.
     *         <ul>
     *         <li>if {@link FileUtils#getFolderName(String)} return null, return false</li>
     *         <li>if target directory already exists, return true</li>
     *         <li>return {@link java.io.File#makeFolder}</li>
     *         </ul>
     */
    public static boolean makeDirs(String filePath) 
    {
        String folderName = getFolderName(filePath);
        if (folderName.isEmpty())
            return false;

        File folder = new File(folderName);
        return (folder.exists() && folder.isDirectory()) ? true : folder.mkdirs();
    }

    /**
     * @param filePath
     * @return
     * @see #makeDirs(String)
     */
    public static boolean makeFolders(String filePath) {
        return makeDirs(filePath);
    }

    /**
     * Indicates if this file represents a file on the underlying file system.
     * 
     * @param filePath
     * @return
     */
    public static boolean isFileExist(String filePath) 
    {
        if (filePath.isEmpty())
            return false;

        File file = new File(filePath);
        return (file.exists() && file.isFile());
    }

    /**
     * Indicates if this file represents a directory on the underlying file system.
     * 
     * @param directoryPath
     * @return
     */
    public static boolean isFolderExist(String directoryPath) 
    {
        if (directoryPath.isEmpty())
            return false;

        File dire = new File(directoryPath);
        return (dire.exists() && dire.isDirectory());
    }

    /**
     * delete file or directory
     * <ul>
     * <li>if path is null or empty, return true</li>
     * <li>if path not exist, return true</li>
     * <li>if path exist, delete recursion. return true</li>
     * <ul>
     * 
     * @param path
     * @return
     */
    public static boolean deleteFile(String path) 
    {
        if (path.isEmpty())
            return true;

        File file = new File(path);
        if (!file.exists()) {
            return true;
        }
        if (file.isFile()) {
            return file.delete();
        }
        if (!file.isDirectory()) {
            return false;
        }
        for (File f : file.listFiles()) {
            if (f.isFile()) {
                f.delete();
            } else if (f.isDirectory()) {
                deleteFile(f.getAbsolutePath());
            }
        }
        return file.delete();
    }

    /**
     * get file size
     * <ul>
     * <li>if path is null or empty, return -1</li>
     * <li>if path exist and it is a file, return file size, else return -1</li>
     * <ul>
     * 
     * @param path
     * @return returns the length of this file in bytes. returns -1 if the file does not exist.
     */
    public static long getFileSize(String path) 
    {
        if (path.isEmpty())
            return -1;

        File file = new File(path);
        return (file.exists() && file.isFile() ? file.length() : -1);
    }
    
    /**
     * Untar the contents of a tar file into the given directory, ignoring the
     * relative pathname in the tar file, and delete the tar file.
     * 
     * Uses jtar: http://code.google.com/p/jtar/
     * 
     * @param tarFile The tar file to be untarred
     * @param destinationDir The directory to untar into
     * @throws IOException
     */
    public static boolean unTarFile(String tarFile, String destFolder)
    {
    	boolean retVal = false;
    	if(!tarFile.isEmpty() && isFileExist(tarFile) && !destFolder.isEmpty())
    	{
    		makeDirs(destFolder);
			try {
				// Create a TarInputStream
				TarInputStream tis = new TarInputStream(new BufferedInputStream(new FileInputStream(tarFile)));
				TarEntry entry;
				while((entry = tis.getNextEntry()) != null) 
				{
					int count;
					byte data[] = new byte[2048];
					String pathName = entry.getName();
					Log.d(TAG, "entry.getName() = "+pathName);
				    String fileName = pathName.substring(pathName.lastIndexOf('/'), pathName.length());
					FileOutputStream fos = new FileOutputStream(destFolder + fileName);
					BufferedOutputStream dest = new BufferedOutputStream(fos);
			
					while((count = tis.read(data)) != -1) {
						dest.write(data, 0, count);
					}
			
					dest.flush();
					dest.close();
				}
				tis.close();
				deleteFile(tarFile);
				retVal = true;
			} catch (FileNotFoundException e) {
				Log.e(TAG, e.getMessage());
				e.printStackTrace();
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
				e.printStackTrace();
			}
    	}
    	return retVal;
    }
    
    public static boolean tarFile(List<String> filesPathList, String destFilePath)
    {
    	boolean retVal = false;
    	if(filesPathList != null && filesPathList.size() > 0 && !destFilePath.isEmpty())
    	{
    		//check fils existence
    		for(String file : filesPathList)
    		{
    			if(!isFileExist(file))
    				return false;
    		}
    		//create folder
    		makeDirs(destFilePath);  		
     	   	try {
     	   		// Output file stream
				FileOutputStream dest = new FileOutputStream(destFilePath);
				// Create a TarOutputStream
		    	TarOutputStream out = new TarOutputStream( new BufferedOutputStream(dest));
		    	for(String file : filesPathList)
		    	{
		    		File fileToTar = new File(file);
		    	    out.putNextEntry(new TarEntry(fileToTar, fileToTar.getName()));
		    	    BufferedInputStream origin = new BufferedInputStream(new FileInputStream( fileToTar ));
		    	    int count;
		    	    byte data[] = new byte[2048];
		    	    while((count = origin.read(data)) != -1) 
		    	    {
		    	         out.write(data, 0, count);
		    	    }
		    	    out.flush();
		    	    origin.close();
		    	    out.close();
		    	    retVal = true;
		    	}
			} catch (FileNotFoundException e) {
				Log.e(TAG, e.getMessage());
				e.printStackTrace();
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
				e.printStackTrace();
			}
    	}
    	return retVal;
    }
}
