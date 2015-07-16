/*
 * Copyright (C) 2013 Paul Burke
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

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Fragment that displays a list of Files in a given path.
 * 
 * @version 2013-12-11
 * @author paulburke (ipaulpro)
 */
public class FileListFragment extends ListFragment implements
        LoaderManager.LoaderCallbacks<List<File>> {

    /**
     * Interface to listen for events.
     */
    public interface Callbacks {
        /**
         * Called when a file is selected from the list.
         *
         * @param file The file selected
         */
        public void onFileSelected(File file);
        public void onFilesSelected(ArrayList<File> files);
    }

    private static final String INST_KEY_FILECHOOSER_MULTI_SELECTION = "InstanceState.filechooserMultiSelection";

    private static final int LOADER_ID = 0;

    private FileListAdapter mAdapter;
    private String mPath;

    private Callbacks mListener;

    /**
     * Create a new instance with the given file path.
     *
     * @param path The absolute path of the file (directory) to display.
     * @return A new Fragment with the given file path.
     */
    public static FileListFragment newInstance(String path) {
        FileListFragment fragment = new FileListFragment();
        Bundle args = new Bundle();
        args.putString(FileChooserActivity.PATH, path);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (Callbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FileListFragment.Callbacks");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new FileListAdapter(getActivity(), mFileSelectCallback);
        mPath = getArguments() != null ? getArguments().getString(
                FileChooserActivity.PATH) : Environment
                .getExternalStorageDirectory().getAbsolutePath();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        setEmptyText(getString(R.string.empty_directory));
        setListAdapter(mAdapter);
        setListShown(false);

        // Restore
        if (savedInstanceState != null) {

            // stop loader in case of rotation during loading to avoid NullPointerException
            if (getLoaderManager().getLoader(LOADER_ID) != null) {
                getLoaderManager().getLoader(LOADER_ID).reset();
            }

            ArrayList<String> filesStr = savedInstanceState.getStringArrayList(INST_KEY_FILECHOOSER_MULTI_SELECTION);
            ArrayList<File> files = new ArrayList<File>(filesStr.size());
            for (String fileStr : filesStr) {
                Uri fileUri = Uri.parse(fileStr);
                files.add(new File(fileUri.getPath()));
            }
            mFileSelectCallback.setFromArrayListSelectionMultipleFiles(files);
            startMultiSelectionModeIfNeeded();
        }

        getLoaderManager().initLoader(LOADER_ID, null, this);

        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<List<File>> onCreateLoader(int id, Bundle args) {
        return new FileLoader(getActivity(), mPath);
    }

    @Override
    public void onLoadFinished(Loader<List<File>> loader, List<File> data) {
        mAdapter.setListItems(data);

        if (isResumed())
            setListShown(true);
        else
            setListShownNoAnimation(true);
    }

    @Override
    public void onLoaderReset(Loader<List<File>> loader) {
        mAdapter.clear();
    }


    /************************/
    /* EVOL MULTI-SELECTION */
    /*  manage action mode  */
    /************************/

    private ActionMode mActionMode;

    private boolean isMultiSelectionModeNeeded() {
        return (mFileSelectCallback != null) && (mFileSelectCallback.getSize() > 0);
    }

    private void startMultiSelectionModeIfNeeded() {
        if (isMultiSelectionModeNeeded()) {
            if (!isMultiSelectionModeEnabled()) {
                mActionMode = getActivity().startActionMode(mActionModeCallback);
                updateMultiSelectionModeView();
            }
        }
    }

    private void updateMultiSelectionModeView() {
        if (isMultiSelectionModeEnabled()) {
            int size = mFileSelectCallback.getSize();
            if (size > 0) {
                mActionMode.setTitle(Integer.toString(size));
            } else {
                closeMultiSelectionMode();
            }
        }
    }

    private void closeMultiSelectionMode() {
        if (isMultiSelectionModeEnabled()) {
            mActionMode.finish();
        }
    }

    private void clearMultiSelectionModeView() {
        mActionMode = null;
        mFileSelectCallback.clear();

        // Clear items view state (force to clear views but does not touch data)
        getListView().setAdapter(getListAdapter());
    }

    protected boolean isMultiSelectionModeEnabled() {
        return (mActionMode != null);
    }

    private FileSelectCallback mFileSelectCallback = new FileSelectCallback() {

        @Override
        public void onSelectFile(File entry, View view) {

            if (!isMultiSelectionModeEnabled()) {

                if(entry.isDirectory()) {
                    mPath = entry.getAbsolutePath();
                    mListener.onFileSelected(entry);
                } else {
                    mSelectionMultipleFiles.add(entry);
                    view.setSelected(true);
                    manageUpload();
                    closeMultiSelectionMode();
                }
            } else {
                // Selection navigation : click => add to selection
                if (view.isSelected()) {
                    // remove from selection
                    mSelectionMultipleFiles.remove(entry);
                    view.setSelected(false);
                } else {
                    // add to selection
                    mSelectionMultipleFiles.add(entry);
                    view.setSelected(true);
                }
                updateMultiSelectionModeView();
            }
        }

        @Override
        public boolean onLongSelectFile(File entry, View view) {
            if (isMultiSelectionModeEnabled()) {
                return false;
            }

            // select first file
            mSelectionMultipleFiles.push(entry);
            view.setSelected(true);
            startMultiSelectionModeIfNeeded();

            return true;
        }
    };


    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        // called when the action mode is created; startActionMode() was called
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.fragment_afilechooser_selection, menu);
            return true;
        }

        @Override
        // the following method is called each time
        // the action mode is shown. Always called after
        // onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        // Return false if nothing is done
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        // called when the user selects a contextual menu item
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if(item.getItemId() == R.id.action_multifile_upload) {
                manageUpload();
                closeMultiSelectionMode();
            } else if(item.getItemId() == R.id.action_multifile_select_all) {
                if(mFileSelectCallback.getSelectionMultipleFiles().size() < mAdapter.getListItems().size()) {
                    selectAll();
                } else {
                    unselectAll();
                }
            }

            return false;
        }

        @Override
        // called when the user exits the action mode
        public void onDestroyActionMode(ActionMode mode) {
            clearMultiSelectionModeView();
        }
    };

    private void manageUpload() {
        mListener.onFilesSelected(mFileSelectCallback.getArrayListOfSelectionMultipleFiles());
    }

    private void selectAll() {

        for(File file : mAdapter.getListItems()) {

            if(!mFileSelectCallback.getArrayListOfSelectionMultipleFiles().contains(file)) {
                mFileSelectCallback.mSelectionMultipleFiles.add(file);
            }
        }
        getListView().setAdapter(getListAdapter());
        updateMultiSelectionModeView();
    }

    private void unselectAll() {
        for(File file : mAdapter.getListItems()) {

            if(mFileSelectCallback.getArrayListOfSelectionMultipleFiles().contains(file)) {
                mFileSelectCallback.mSelectionMultipleFiles.remove(file);
            }
        }
        closeMultiSelectionMode();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        ArrayList<String> filesString = new ArrayList<String>();
        for(File file : this.mFileSelectCallback.getArrayListOfSelectionMultipleFiles()) {
            Uri uri = Uri.fromFile(file);
            filesString.add(uri.toString());
        }
        outState.putStringArrayList(INST_KEY_FILECHOOSER_MULTI_SELECTION, filesString);
    }
}
