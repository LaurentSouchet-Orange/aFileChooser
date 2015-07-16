/*
 * Copyright (C) 2012 Paul Burke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ipaulpro.afilechooser;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * List adapter for Files.
 * 
 * @version 2013-12-11
 * @author paulburke (ipaulpro)
 */
public class FileListAdapter extends BaseAdapter {

    private final static int ICON_FOLDER = R.drawable.explorer_list_file;
    private final static int ICON_FILE = R.drawable.ic_filedetails_type_default;

    private final LayoutInflater mInflater;

    private List<File> mData = new ArrayList<File>();

    private FileSelectCallback mFileSelectCallback;

    public FileListAdapter(Context context, FileSelectCallback fileSelectCallback) {
        mInflater = LayoutInflater.from(context);
        mFileSelectCallback = fileSelectCallback;
    }

    /**
     * Retourne les éléments sélectionnés
     *
     * @return
     */
    public FileSelectCallback getFileSelectCallback() {
        return mFileSelectCallback;
    }

    public void add(File file) {
        mData.add(file);
        notifyDataSetChanged();
    }

    public void remove(File file) {
        mData.remove(file);
        notifyDataSetChanged();
    }

    public void insert(File file, int index) {
        mData.add(index, file);
        notifyDataSetChanged();
    }

    public void clear() {
        mData.clear();
        notifyDataSetChanged();
    }

    @Override
    public File getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    public List<File> getListItems() {
        return mData;
    }

    /**
     * Set the list items without notifying on the clear. This prevents loss of
     * scroll position.
     *
     * @param data
     */
    public void setListItems(List<File> data) {
        mData = data;
        notifyDataSetChanged();
    }

    private class ViewHolder {
        public TextView row;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // Get the file at the current position
        final File file = getItem(position);
        boolean selectedState = mFileSelectCallback.getSelectionMultipleFiles().contains(file);

        // Re-use component
        if(convertView == null) {
            convertView = mInflater.inflate(R.layout.file, parent, false);
            ViewHolder viewHolder = new ViewHolder();

            viewHolder.row = (TextView)convertView.findViewById(R.id.fileChooserItem);

            convertView.setTag(viewHolder);
        }

        // Unwrap view holder
        ViewHolder viewHolder = (ViewHolder) convertView.getTag();

        viewHolder.row.setSelected(selectedState);

        // Set the TextView as the file name
        viewHolder.row.setText(file.getName());

        // If the item is not a directory, use the file icon
        int icon = file.isDirectory() ? ICON_FOLDER : ICON_FILE;
        viewHolder.row.setCompoundDrawablesWithIntrinsicBounds(icon, 0, 0, 0);

        // Manage click listeners
        viewHolder.row.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getFileSelectCallback().onSelectFile(file, v);
            }
        });

        viewHolder.row.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                return getFileSelectCallback().onLongSelectFile(file, v);
            }
        });
        return convertView;
    }

}