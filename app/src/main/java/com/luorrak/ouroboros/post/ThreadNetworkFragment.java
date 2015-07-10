package com.luorrak.ouroboros.post;

import android.app.Activity;
import android.app.Fragment;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.luorrak.ouroboros.api.JsonParser;
import com.luorrak.ouroboros.util.DbContract;
import com.luorrak.ouroboros.util.InfiniteDbHelper;

/**
 * Created by elements on 7/10/15.
 */
public class ThreadNetworkFragment extends Fragment {
    Activity activity;
    InsertThreadIntoDatabaseTask insertThreadIntoDatabaseTask;

    public void beginTask(JsonObject jsonObject, InfiniteDbHelper infiniteDbHelper, String boardName, String resto, ThreadAdapter threadAdapter){
        insertThreadIntoDatabaseTask = new InsertThreadIntoDatabaseTask(activity, infiniteDbHelper, boardName, resto, threadAdapter);
        insertThreadIntoDatabaseTask.execute(jsonObject);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
        if (insertThreadIntoDatabaseTask != null){
            insertThreadIntoDatabaseTask.onAttach(activity);;
        }

    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (insertThreadIntoDatabaseTask != null){
            insertThreadIntoDatabaseTask.onDetach();
        }
    }

    public class InsertThreadIntoDatabaseTask extends AsyncTask<JsonObject, Void, Void> {
        private Activity activity;
        private InfiniteDbHelper infiniteDbHelper;
        private String boardName;
        private String resto;
        private ThreadAdapter threadAdapter;

        public InsertThreadIntoDatabaseTask(Activity activity, InfiniteDbHelper infiniteDbHelper, String boardName, String resto, ThreadAdapter threadAdapter) {
            onAttach(activity);
            this.infiniteDbHelper = infiniteDbHelper;
            this.boardName = boardName;
            this.resto = resto;
            this.threadAdapter = threadAdapter;
        }

        public void onDetach(){
            this.activity = null;
        }

        public void onAttach(Activity activity){;
            this.activity = activity;
        }

        @Override
        protected Void doInBackground(JsonObject... params) {
            JsonParser jsonParser = new JsonParser();
            JsonArray posts = params[0].getAsJsonArray("posts");
            for (JsonElement postElement : posts) {
                JsonObject post = postElement.getAsJsonObject();

                infiniteDbHelper.insertThreadEntry(
                        boardName,
                        jsonParser.getThreadResto(post),
                        jsonParser.getThreadNo(post),
                        jsonParser.getThreadFilename(post),
                        jsonParser.getThreadTim(post),
                        jsonParser.getThreadExt(post),
                        jsonParser.getThreadSub(post),
                        jsonParser.getThreadCom(post),
                        jsonParser.getThreadEmail(post),
                        jsonParser.getThreadName(post),
                        jsonParser.getThreadTrip(post),
                        jsonParser.getThreadTime(post),
                        jsonParser.getThreadLastModified(post),
                        jsonParser.getThreadId(post),
                        jsonParser.getThreadEmbed(post),
                        jsonParser.getThreadImageHeight(post),
                        jsonParser.getThreadImageWidth(post)
                );
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Cursor cursor = infiniteDbHelper.getThreadCursor(resto);
            String threadSubject = cursor.getString(cursor.getColumnIndex(DbContract.ThreadEntry.COLUMN_THREAD_SUB));
            activity.setTitle(threadSubject != null ? threadSubject : "/" + boardName + "/" + resto);
            cursor.close();

            threadAdapter.changeCursor(infiniteDbHelper.getThreadCursor(resto));
        }
    }
}
