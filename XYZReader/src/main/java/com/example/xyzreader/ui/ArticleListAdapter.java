package com.example.xyzreader.ui;

import android.app.ActivityOptions;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by rafae on 26/10/2017.
 */

public class ArticleListAdapter extends RecyclerView.Adapter<ArticleListAdapter.ViewHolder> {

    private static final String TAG = ArticleListAdapter.class.toString();

    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.sss");
    // Use default locale format
    private SimpleDateFormat outputFormat = new SimpleDateFormat();
    // Most time functions can only handle 1902 - 2037
    private GregorianCalendar START_OF_EPOCH = new GregorianCalendar(2, 1, 1);

    private Cursor mCursor;
    private AppCompatActivity mActivity;

    public ArticleListAdapter(Cursor cursor, AppCompatActivity activity) {
        mCursor = cursor;
        mActivity = activity;
    }

    @Override
    public long getItemId(int position) {
        mCursor.moveToPosition(position);
        return mCursor.getLong(ArticleLoader.Query._ID);
    }

    @Override
    public ArticleListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_article, parent, false);
        return new ViewHolder(view);
    }

    private Date parsePublishedDate() {
        try {
            String date = mCursor.getString(ArticleLoader.Query.PUBLISHED_DATE);
            return dateFormat.parse(date);
        } catch (ParseException ex) {
            Log.e(TAG, ex.getMessage());
            Log.i(TAG, "passing today's date");
            return new Date();
        }
    }

    @Override
    public void onBindViewHolder(final ArticleListAdapter.ViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        // Set up the title
        holder.titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));

        // Set up the date
        Date publishedDate = parsePublishedDate();
        if (!publishedDate.before(START_OF_EPOCH.getTime())) {
            holder.subtitleView.setText(Html.fromHtml(
                    DateUtils.getRelativeTimeSpanString(
                            publishedDate.getTime(),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + "<br/>" + " by "
                            + mCursor.getString(ArticleLoader.Query.AUTHOR)));
        } else {
            holder.subtitleView.setText(Html.fromHtml(
                    outputFormat.format(publishedDate)
                            + "<br/>" + " by "
                            + mCursor.getString(ArticleLoader.Query.AUTHOR)));
        }

        // Set up the thubnail
        holder.thumbnailView.setImageUrl(
                mCursor.getString(ArticleLoader.Query.THUMB_URL),
                ImageLoaderHelper.getInstance(holder.itemView.getContext()).getImageLoader());
        holder.thumbnailView.setAspectRatio(mCursor.getFloat(ArticleLoader.Query.ASPECT_RATIO));
        Glide.with(holder.thumbnailView.getContext())
                .load(mCursor.getString(ArticleLoader.Query.THUMB_URL))
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        // Set up the card background color relative to image
                        Bitmap bitmap = ((BitmapDrawable) resource).getBitmap();
                        Palette palette = Palette.from(bitmap).generate();
                        int defaultColor = 0xFF333333;
                        int color = palette.getDarkMutedColor(defaultColor);
                        holder.articleView.setCardBackgroundColor(color);
                        return true;
                    }
                })
                .into(holder.thumbnailView);

        // Set up the click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle bundle = ActivityOptions.makeSceneTransitionAnimation(mActivity).toBundle();
                mActivity.startActivity(new Intent(Intent.ACTION_VIEW, ItemsContract.Items.buildItemUri(getItemId(holder.getAdapterPosition()))), bundle);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        DynamicHeightNetworkImageView thumbnailView;
        TextView titleView;
        TextView subtitleView;
        CardView articleView;

        ViewHolder(View view) {
            super(view);
            thumbnailView = view.findViewById(R.id.thumbnail);
            titleView = view.findViewById(R.id.article_title);
            subtitleView = view.findViewById(R.id.article_subtitle);
            articleView = view.findViewById(R.id.card_article);
        }
    }
}