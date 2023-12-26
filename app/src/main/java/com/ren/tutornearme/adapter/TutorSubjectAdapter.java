package com.ren.tutornearme.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.ren.tutornearme.R;
import com.ren.tutornearme.model.SubjectInfo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class TutorSubjectAdapter extends RecyclerView.Adapter<TutorSubjectAdapter.SubjectViewHolder>{

    private final Context context;
    private View view;

    private ArrayList<SubjectInfo> subjectInfoArrayList = new ArrayList<>();
    private final SimpleDateFormat dateTimeFormatter =
            new SimpleDateFormat( "dd-MMM-yyyy, hh:mm" , Locale.ENGLISH);

    public TutorSubjectAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(context).inflate(R.layout.tutor_subject_row, parent, false);
        return new SubjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        SubjectInfo subjectInfo = subjectInfoArrayList.get(position);
        holder.subjectNameTextView.setText(subjectInfo.getName());
        holder.subjectDescriptionTextView.setText(subjectInfo.getDescription());
        holder.updatedDateTextView
                .setText(dateTimeFormatter.format(new Date(subjectInfo.getUpdatedDate())));
        holder.logoImageView.setBackgroundResource(R.mipmap.ic_logo_round);
    }

    @Override
    public int getItemCount() {
        return subjectInfoArrayList.size();
    }

    public static class SubjectViewHolder extends RecyclerView.ViewHolder {

        private final TextView subjectNameTextView, subjectDescriptionTextView, updatedDateTextView;
        private final ImageView logoImageView;
        private final CardView rowCardView;
        public SubjectViewHolder(@NonNull View itemView) {
            super(itemView);

            subjectNameTextView = itemView.findViewById(R.id.tutor_subject_name_textView);
            subjectDescriptionTextView = itemView.findViewById(R.id.tutor_subject_description_textView);
            updatedDateTextView = itemView.findViewById(R.id.tutor_subject_updated_date);
            logoImageView = itemView.findViewById(R.id.tutor_subject_logo_imageView);
            rowCardView = itemView.findViewById(R.id.tutor_subject_row_cardView);
        }
    }

    public void setSubjectList(ArrayList<SubjectInfo> filteredList) {
        subjectInfoArrayList = filteredList;
        notifyDataSetChanged();
    }
}
