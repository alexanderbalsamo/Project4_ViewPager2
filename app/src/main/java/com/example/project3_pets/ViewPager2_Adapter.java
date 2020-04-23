package com.example.project3_pets;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class ViewPager2_Adapter extends RecyclerView.Adapter {
    private static final String TAG = "Balsamo";
    private final Context ctx;
    private final LayoutInflater li;
    private JSONArray jsonInfo;
    private String userURL;
    //TODO: get images from JSON info
    private List<String> file_resources;
    private List<String> name_resources;
    private int[] image_resources = {R.drawable.error, R. drawable.error2};

    public void passJSONInfo(JSONArray json, String url) throws JSONException {
        jsonInfo = json;
        userURL = url;
        notifyDataSetChanged();
        //DEBUG Statements
        Log.d(TAG, "json: "+ jsonInfo);
        Log.d(TAG, "jsonLength: "+ jsonInfo.length());
        Log.d(TAG, "url: "+ userURL);
        if(jsonInfo != null) {
            getImageResource();
        }
    }

    private void getImageResource() throws JSONException {
        String jsonPets = "pets.json";
        file_resources = new ArrayList<String>();
        for(int i = 0; i< jsonInfo.length(); i++){
            String imageFile = jsonInfo.getJSONObject(i).getString("file");
            String imageURL = userURL.substring(i, userURL.length() - jsonPets.length()) + imageFile;
            file_resources.add(imageURL);
        }
        Log.d(TAG, "files: "+ file_resources);
    }



    class PagerViewHolder extends RecyclerView.ViewHolder {
        private static final int UNINITIALIZED = -1;
        ImageView iv;
        TextView petName;
        TextView textInfo;
        int position=UNINITIALIZED;     //start off uninitialized, set it when we are populating
                                        //with a view in onBindViewHolder

        public PagerViewHolder(@NonNull View itemView) {
            super(itemView);
            iv = (ImageView)itemView.findViewById(R.id.imageView);
            petName = (TextView)itemView.findViewById(R.id.tvPetName);
            textInfo = (TextView)itemView.findViewById(R.id.tvInfo);
        }
    }

    private class GetImage extends AsyncTask<Void, Void, Void> {
        //ref to a viewholder
        private PagerViewHolder myVh;

        //since myVH may be recycled and reused
        //we have to verify that the result we are returning
        //is still what the viewholder wants
        private int original_position;

        public GetImage(PagerViewHolder myVh) {
            //hold on to a reference to this viewholder
            //note that its contents (specifically iv) may change
            //iff the viewholder is recycled
            this.myVh = myVh;
            //make a copy to compare later, once we have the image
            this.original_position = myVh.position;
        }
        @Override
        protected Void doInBackground(Void... params) {
            return null;
        }
        @Override
        protected void onPostExecute(Void param) {
             //got a result, if the following are NOT equal
            // then the view has been recycled and is being used by another
            // number DO NOT MODIFY
            if (this.myVh.position == this.original_position){
                //still valid
                //set the result on the main thread
                //TODO: set the image using a bitmap?? instead of setImageResource?
                //myVh.iv.setImageUrl();

                //myVh.iv.setImageResource(image_resources[this.myVh.position]);
                myVh.textInfo.setText("");
                //TODO: add setText to fill in petName from JSON info
                //myVh.petName.setText(image_resources[this.]);
            }
            else
                Toast.makeText(ViewPager2_Adapter.this.ctx,"YIKES! Recycler view reused, my result is useless", Toast.LENGTH_SHORT).show();
        }
    }

    public ViewPager2_Adapter(Context ctx){
        this.ctx=ctx;

        //will use this to ceate swipe_layouts in onCreateViewHolder
        li=(LayoutInflater)ctx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //call this when we need to create a brand new PagerViewHolder
        View view = li.inflate(R.layout.swipe_layout, parent, false);
        return new PagerViewHolder(view);   //the new one
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        //passing in an existing instance, reuse the internal resources
        //pass our data to our ViewHolder.
        PagerViewHolder viewHolder = (PagerViewHolder) holder;

        //set to some default image
        viewHolder.iv.setImageResource(R.drawable.error);
        viewHolder.petName.setText("");
        viewHolder.textInfo.setText(R.string.getInfoString);
        viewHolder.position=position;       //remember which image this view is bound to

        //launch a thread to 'retrieve' the image
        GetImage myTask = new GetImage(viewHolder);
        myTask.execute();
    }

    @Override
    public int getItemCount() {
        //the size of the collection that contains the items we want to display
        return image_resources.length;

        //TODO: add this back in
        //return imageInfo.length();
    }
}
