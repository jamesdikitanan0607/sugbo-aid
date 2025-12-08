package com.sugboaid.donation.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sugboaid.donation.R;
import com.sugboaid.models.User;

import java.util.List;

public class UserManagementAdapter extends RecyclerView.Adapter<UserManagementAdapter.UserViewHolder> {
    private final List<User> users;
    private final OnUserActionListener listener;

    public interface OnUserActionListener {
        void onRoleChange(User user);
        void onStatusToggle(User user);
    }

    public UserManagementAdapter(List<User> users, OnUserActionListener listener) {
        this.users = users;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_management, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.bind(users.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return users != null ? users.size() : 0;
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvUserName;
        private final TextView tvUserEmail;
        private final TextView tvUserRole;
        private final Button btnChangeRole;
        private final Switch switchUserStatus;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserName = itemView.findViewById(R.id.tv_user_name);
            tvUserEmail = itemView.findViewById(R.id.tv_user_email);
            tvUserRole = itemView.findViewById(R.id.tv_user_role);
            btnChangeRole = itemView.findViewById(R.id.btn_change_role);
            switchUserStatus = itemView.findViewById(R.id.switch_user_status);
        }

        public void bind(User user, OnUserActionListener listener) {
            if (user == null) return;
            tvUserName.setText(user.getName());
            tvUserEmail.setText(user.getEmail());
            tvUserRole.setText(user.getRole());

            int color;
            switch (user.getRole()) {
                case User.ROLE_ADMIN:
                    color = itemView.getContext().getColor(R.color.primary_blue);
                    break;
                case User.ROLE_DONOR:
                    color = itemView.getContext().getColor(R.color.success_green);
                    break;
                case User.ROLE_RECIPIENT:
                    color = itemView.getContext().getColor(R.color.warning_color);
                    break;
                default:
                    color = itemView.getContext().getColor(R.color.text_secondary);
            }
            tvUserRole.setTextColor(color);

            btnChangeRole.setOnClickListener(v -> {
                if (listener != null) listener.onRoleChange(user);
            });

            switchUserStatus.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) listener.onStatusToggle(user);
            });
        }
    }
}
