package com.ren.tutornearme.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.LifecycleOwner;
import androidx.recyclerview.widget.RecyclerView;

import com.ren.tutornearme.R;
import com.ren.tutornearme.model.TutorSubject;
import com.ren.tutornearme.ui.subject.SubjectViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.SubjectViewHolder>{

    private final Context context;
    private View view;

    private ArrayList<TutorSubject> tutorSubjectArrayList = new ArrayList<>();
    private final SimpleDateFormat dateTimeFormatter =
            new SimpleDateFormat( "dd-MMM-yyyy, hh:mm" , Locale.ENGLISH);
    private final SubjectViewModel subjectViewModel;
    private final LifecycleOwner lifecycleOwner;

    public SubjectAdapter(SubjectViewModel subjectViewModel,
                          LifecycleOwner lifecycleOwner, Context context) {
        this.context = context;
        this.lifecycleOwner = lifecycleOwner;
        this.subjectViewModel = subjectViewModel;

    }
    @NonNull
    @Override
    public SubjectViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(context).inflate(R.layout.subject_list_row, parent, false);
        return new SubjectViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectViewHolder holder, int position) {
        TutorSubject tutorSubject = tutorSubjectArrayList.get(position);
        holder.subjectNameTextView.setText(tutorSubject.getName());
        holder.subjectDescriptionTextView.setText(tutorSubject.getDescription());
        holder.updatedDateTextView
                .setText(dateTimeFormatter.format(new Date(tutorSubject.getUpdatedDate())));
        holder.logoImageView.setBackgroundResource(R.mipmap.ic_logo_round);

        /*holder.deleteImageView.setOnClickListener(view ->
                createDeleteDialogBuilder(holder.getAdapterPosition()));

        holder.rowCardView.setOnClickListener(view -> {
            createUpdateDialogBuilder(holder.getAdapterPosition());
        });*/
    }

    @Override
    public int getItemCount() {
        return tutorSubjectArrayList.size();
    }

    public static class SubjectViewHolder extends RecyclerView.ViewHolder {

        private final TextView subjectNameTextView, subjectDescriptionTextView, updatedDateTextView;
        private final ImageView deleteImageView, logoImageView;
        private final CardView rowCardView;
        public SubjectViewHolder(@NonNull View itemView) {
            super(itemView);

            subjectNameTextView = itemView.findViewById(R.id.subject_name_textView);
            subjectDescriptionTextView = itemView.findViewById(R.id.subject_description_textView);
            updatedDateTextView = itemView.findViewById(R.id.subject_updated_date);
            deleteImageView = itemView.findViewById(R.id.subject_delete_imageView);
            logoImageView = itemView.findViewById(R.id.subject_logo_imageView);
            rowCardView = itemView.findViewById(R.id.subject_row_cardView);
        }
    }

    public void setSubjectList(ArrayList<TutorSubject> filteredList) {
        tutorSubjectArrayList = filteredList;
        notifyDataSetChanged();
    }
}
