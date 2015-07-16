package com.ipaulpro.afilechooser;

import android.view.View;

import java.util.ArrayList;
import java.util.Stack;

import java.io.File;


public abstract class FileSelectCallback {

    // Stack pour garder l'ordre
    protected Stack<File> mSelectionMultipleFiles;

    public FileSelectCallback() {
        mSelectionMultipleFiles = new Stack<File>();
    }

    public abstract void onSelectFile(File entry, View view);

    public abstract boolean onLongSelectFile(File entry, View view);

    public Stack<File> getSelectionMultipleFiles() {
        return mSelectionMultipleFiles;
    }


    public ArrayList<File> getArrayListOfSelectionMultipleFiles() {
        ArrayList<File> tempArrayList = new ArrayList<File>(mSelectionMultipleFiles.size());
        tempArrayList.addAll(mSelectionMultipleFiles);
        return tempArrayList;
    }

    /**
     * Retourne le nombre d'élément sélectionnés
     */
    public int getSize() {
        if (mSelectionMultipleFiles == null) {
            return 0;
        } else {
            return mSelectionMultipleFiles.size();
        }
    }

    public void setFromArrayListSelectionMultipleFiles(ArrayList<File> selectionMultipleFiles) {
        mSelectionMultipleFiles.clear();
        mSelectionMultipleFiles.addAll(selectionMultipleFiles);
    }

    public void clear() {
        mSelectionMultipleFiles.clear();
    }
}
