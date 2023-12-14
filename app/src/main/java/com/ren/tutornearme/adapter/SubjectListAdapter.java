package com.ren.tutornearme.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.LifecycleOwner;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;

import com.ren.tutornearme.R;
import com.ren.tutornearme.model.SubjectInfo;
import com.ren.tutornearme.ui.subject.subject_list.SubjectListViewModel;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class SubjectListAdapter extends RecyclerView.Adapter<SubjectListAdapter.SubjectListViewHolder>{
    private final Context context;
    private View view;
    private final NavController navController;
    private ArrayList<SubjectInfo> subjectInfoArrayList = new ArrayList<>();
    private final SimpleDateFormat dateTimeFormatter =
            new SimpleDateFormat( "dd-MMM-yyyy, hh:mm" , Locale.ENGLISH);

    private final SubjectListViewModel subjectListViewModel;
    private final LifecycleOwner lifecycleOwner;

    public SubjectListAdapter(SubjectListViewModel subjectViewModel,
                          LifecycleOwner lifecycleOwner, Context context, NavController navController) {
        this.context = context;
        this.lifecycleOwner = lifecycleOwner;
        this.subjectListViewModel = subjectViewModel;
        this.navController = navController;
    }


    @NonNull
    @Override
    public SubjectListAdapter.SubjectListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        view = LayoutInflater.from(context).inflate(R.layout.subject_list_row, parent, false);
        return new SubjectListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubjectListAdapter.SubjectListViewHolder holder, int position) {
        SubjectInfo subjectInfo = subjectInfoArrayList.get(position);
        holder.subjectNameTextView.setText(subjectInfo.getName());
        holder.subjectDescriptionTextView.setText(subjectInfo.getDescription());
        holder.updatedDateTextView
                .setText(dateTimeFormatter.format(new Date(subjectInfo.getUpdatedDate())));
        holder.rowCardView.setOnClickListener(view -> {
            navController.navigate(R.id.action_subjectListFragment_to_subjectFilesFragment);
        });
    }

    @Override
    public int getItemCount() {
        return subjectInfoArrayList.size();
    }

    public static class SubjectListViewHolder extends RecyclerView.ViewHolder {
        private final TextView subjectNameTextView, subjectDescriptionTextView, updatedDateTextView;
        private final CardView rowCardView;
        public SubjectListViewHolder(@NonNull View itemView) {
            super(itemView);

            subjectNameTextView = itemView.findViewById(R.id.subject_list_name_textView);
            subjectDescriptionTextView = itemView.findViewById(R.id.subject_list_description_textView);
            updatedDateTextView = itemView.findViewById(R.id.subject_list_updated_date);
            rowCardView = itemView.findViewById(R.id.subject_list_row_cardView);
        }
    }

    public void setSubjectList(ArrayList<SubjectInfo> filteredList) {
        subjectInfoArrayList = filteredList;
        notifyDataSetChanged();
    }
}
