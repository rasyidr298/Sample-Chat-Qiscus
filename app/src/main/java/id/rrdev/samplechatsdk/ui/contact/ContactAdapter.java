package id.rrdev.samplechatsdk.ui.contact;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;;
import com.bumptech.glide.request.RequestOptions;
import com.qiscus.nirmana.Nirmana;
import com.qiscus.sdk.chat.core.data.model.QiscusComment;

import java.util.ArrayList;
import java.util.List;

import id.rrdev.samplechatsdk.R;
import id.rrdev.samplechatsdk.data.model.User;
import id.rrdev.samplechatsdk.util.DateUtil;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {

    private List<User> userList = new ArrayList<>();
    private Callback.ItemClick itemClick;
    private Callback.ItemLongClick itemLongClick;

    public void submitList(List<User> list){
        userList.clear();
        userList.addAll(list);
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(Callback.ItemClick callback){
        itemClick = callback;
    }

    public void setOnItemLongClickListener(Callback.ItemLongClick callback){
        itemLongClick = callback;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_contact, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(userList.get(position));

        if (itemClick != null){
            holder.itemView.setOnClickListener(v -> itemClick.onItemClick(v, userList.get(position), position));
        }

        if (itemLongClick != null){
            holder.itemView.setOnLongClickListener(v -> itemLongClick.onItemLongClick(v, userList.get(position), position));
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView avatar;
        private TextView name;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            name = itemView.findViewById(R.id.name);
        }

        public void bind(User user){
            Nirmana.getInstance().get()
                    .setDefaultRequestOptions(new RequestOptions()
                            .placeholder(R.drawable.ic_qiscus_avatar)
                            .error(R.drawable.ic_qiscus_avatar)
                            .dontAnimate())
                    .load(user.getAvatarUrl())
                    .into(avatar);

            name.setText(user.getName());

        }
    }

    interface Callback {
        interface ItemClick {
            void onItemClick(View view, User user, int position);
        }
        interface ItemLongClick {
            Boolean onItemLongClick(View view, User user, int position);
        }
    }
}
