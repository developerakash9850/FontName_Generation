package com.mindorks.framework.fontname_generation;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class SaveImage 
{
	public boolean storeImage(Bitmap imageData, String filename) 
	{
		String iconsStoragePath = Environment.getExternalStorageDirectory() + "/myImages";
		File sdIconStorageDir = new File(iconsStoragePath);
		sdIconStorageDir.mkdirs();
		try {
				String filePath = sdIconStorageDir + "/" + filename + ".png";
				FileOutputStream fileOutputStream = new FileOutputStream(filePath);
				BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);
				imageData.compress(CompressFormat.PNG, 100, bos);
				bos.flush();
				bos.close();
			} 
		catch (FileNotFoundException e) 
		{
			Log.w("TAG", "Error saving image file: " + e.getMessage());
			return false;
		} 
		catch (IOException e) 
		{
			Log.w("TAG", "Error saving image file: " + e.getMessage());
			return false;
		}
		return true;
	}
}