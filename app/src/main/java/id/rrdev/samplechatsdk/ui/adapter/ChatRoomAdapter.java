package id.rrdev.samplechatsdk.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.qiscus.sdk.chat.core.data.model.QiscusChatRoom;
import com.qiscus.sdk.chat.core.data.model.QiscusComment;

import java.util.ArrayList;
import java.util.List;

import id.rrdev.samplechatsdk.R;
import id.rrdev.samplechatsdk.util.DateUtil;

public class ChatRoomAdapter extends RecyclerView.Adapter<ChatRoomAdapter.ViewHolder> {

    private List<QiscusChatRoom> chatRooms = new ArrayList<>();
    private Callback.ItemClick itemClick;
    private Callback.ItemLongClick itemLongClick;

    public void submitList(List<QiscusChatRoom> list){
        chatRooms.clear();
        chatRooms.addAll(list);
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
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_room, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(chatRooms.get(position));

        if (itemClick != null){
            holder.itemView.setOnClickListener(v -> itemClick.onItemClick(v, chatRooms.get(position), position));
        }

        if (itemLongClick != null){
            holder.itemView.setOnLongClickListener(v -> itemLongClick.onItemLongClick(v, chatRooms.get(position), position));
        }
    }

    @Override
    public int getItemCount() {
        return chatRooms.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView avatar;
        private TextView name, lastMessage, tv_unread_count, tv_time;
        private FrameLayout layout_unread_count;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            name = itemView.findViewById(R.id.name);
            lastMessage = itemView.findViewById(R.id.tv_last_message);
            tv_unread_count = itemView.findViewById(R.id.tv_unread_count);
            tv_time = itemView.findViewById(R.id.tv_time);
            layout_unread_count = itemView.findViewById(R.id.layout_unread_count);
        }

        public void bind(QiscusChatRoom chatRoom){

            Glide.with(itemView).load(chatRoom.getAvatarUrl()).into(avatar);

            name.setText(chatRoom.getName());

            QiscusComment lastComment = chatRoom.getLastComment();
            if (lastComment != null && lastComment.getId() > 0) {
                if (lastComment.getSender() != null) {
                    String lastMessageText = lastComment.isMyComment() ? "You: " : lastComment.getSender().split(" ")[0] + ": ";
                    lastMessageText += chatRoom.getLastComment().getType() == QiscusComment.Type.IMAGE
                            ? "\uD83D\uDCF7 send an image" : lastComment.getMessage();
                    lastMessage.setText(lastMessageText);
                }else{
                    String lastMessageText = "";
                    lastMessageText += chatRoom.getLastComment().getType() == QiscusComment.Type.IMAGE
                            ? "\uD83D\uDCF7 send an image" : lastComment.getMessage();
                    lastMessage.setText(lastMessageText);
                }

                tv_time.setText(DateUtil.getLastMessageTimestamp(lastComment.getTime()));
            } else {
                lastMessage.setText("");
                tv_time.setText("");
            }

            tv_unread_count.setText(String.format("%d", chatRoom.getUnreadCount()));
            if (chatRoom.getUnreadCount() == 0) {
                layout_unread_count.setVisibility(View.GONE);
            } else {
                layout_unread_count.setVisibility(View.VISIBLE);
            }
        }
    }

    interface Callback {
        interface ItemClick {
            void onItemClick(View view, QiscusChatRoom qiscusChatRoom, int position);
        }
        interface ItemLongClick {
            Boolean onItemLongClick(View view, QiscusChatRoom qiscusChatRoom, int position);
        }
    }
}
