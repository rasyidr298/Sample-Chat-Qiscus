package id.rrdev.samplechatsdk.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.qiscus.nirmana.Nirmana;
import com.qiscus.sdk.chat.core.QiscusCore;
import com.qiscus.sdk.chat.core.data.model.QiscusChatRoom;
import com.qiscus.sdk.chat.core.data.model.QiscusComment;
import com.qiscus.sdk.chat.core.util.QiscusAndroidUtil;
import com.qiscus.sdk.chat.core.util.QiscusDateUtil;

import java.util.List;

import id.rrdev.samplechatsdk.R;
import id.rrdev.samplechatsdk.util.DateUtil;

public class CommentsAdapter extends SortedRecyclerViewAdapter<QiscusComment, CommentsAdapter.VH> {

    private static final int TYPE_MY_TEXT = 1;
    private static final int TYPE_OPPONENT_TEXT = 2;
    private Context context;

    public CommentsAdapter(Context context) {
        this.context = context;
    }

    public void addOrUpdate(List<QiscusComment> comments) {
        for (QiscusComment comment : comments) {
            int index = findPosition(comment);
            if (index == -1) {
                getData().add(comment);
            } else {
                getData().updateItemAt(index, comment);
            }
        }
        notifyDataSetChanged();
    }

    public void addOrUpdate(QiscusComment comment) {
        int index = findPosition(comment);
        if (index == -1) {
            getData().add(comment);
        } else {
            getData().updateItemAt(index, comment);
        }
        notifyDataSetChanged();
    }

    @Override
    protected Class<QiscusComment> getItemClass() {
        return QiscusComment.class;
    }

    @Override
    protected int compare(QiscusComment item1, QiscusComment item2) {
        if (item2.equals(item1)) {
            //Same comments
            return 0;
        } else if (item2.getId() == -1 && item1.getId() == -1) {
            //Not completed comments
            return item2.getTime().compareTo(item1.getTime());
        } else if (item2.getId() != -1 && item1.getId() != -1) {
            //Completed comments
            return QiscusAndroidUtil.compare(item2.getId(), item1.getId());
        } else if (item2.getId() == -1) {
            return 1;
        } else if (item1.getId() == -1) {
            return -1;
        }
        return item2.getTime().compareTo(item1.getTime());
    }

    @Override
    public int getItemViewType(int position) {
        QiscusComment comment = getData().get(position);
        return comment.isMyComment() ? TYPE_MY_TEXT : TYPE_OPPONENT_TEXT;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new TextVH(getView(parent, viewType));
    }

    private View getView(ViewGroup parent, int viewType){
        switch (viewType) {
            case TYPE_MY_TEXT:
                return LayoutInflater.from(context).inflate(R.layout.item_my_text_comment, parent, false);
            case TYPE_OPPONENT_TEXT:
            default:
                return LayoutInflater.from(context).inflate(R.layout.item_opponent_text_comment, parent, false);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        holder.bind(getData().get(position));
        holder.position = position;

        if (position == getData().size() - 1) {
            holder.setNeedToShowDate(true);
        } else {
            holder.setNeedToShowDate(!QiscusDateUtil.isDateEqualIgnoreTime(getData().get(position).getTime(),
                    getData().get(position + 1).getTime()));
        }
    }

    static class VH extends RecyclerView.ViewHolder{
        private final ImageView avatar, state;
        private final TextView sender, date, dateOfMessage;

        private final int pendingStateColor, readStateColor, failedStateColor;
        public int position = 0;

        public VH(View itemView) {
            super(itemView);
            avatar = itemView.findViewById(R.id.avatar);
            sender = itemView.findViewById(R.id.sender);
            date = itemView.findViewById(R.id.date);
            dateOfMessage = itemView.findViewById(R.id.dateOfMessage);
            state = itemView.findViewById(R.id.state);

            pendingStateColor = ContextCompat.getColor(itemView.getContext(), R.color.pending_message);
            readStateColor = ContextCompat.getColor(itemView.getContext(), R.color.read_message);
            failedStateColor = ContextCompat.getColor(itemView.getContext(), R.color.qiscus_red);
        }

        private void renderState(QiscusComment comment) {
            if (state != null) {
                switch (comment.getState()) {
                    case QiscusComment.STATE_PENDING:
                    case QiscusComment.STATE_SENDING:
                        state.setColorFilter(pendingStateColor);
                        state.setImageResource(R.drawable.ic_qiscus_info_time);
                        break;
                    case QiscusComment.STATE_ON_QISCUS:
                        state.setColorFilter(pendingStateColor);
                        state.setImageResource(R.drawable.ic_qiscus_sending);
                        break;
                    case QiscusComment.STATE_DELIVERED:
                        state.setColorFilter(pendingStateColor);
                        state.setImageResource(R.drawable.ic_qiscus_read);
                        break;
                    case QiscusComment.STATE_READ:
                        state.setColorFilter(readStateColor);
                        state.setImageResource(R.drawable.ic_qiscus_read);
                        break;
                    case QiscusComment.STATE_FAILED:
                        state.setColorFilter(failedStateColor);
                        state.setImageResource(R.drawable.ic_qiscus_sending_failed);
                        break;
                }
            }
        }

        void setNeedToShowDate(Boolean showDate) {
            if (showDate) {
                if (dateOfMessage != null) {
                    dateOfMessage.setVisibility(View.VISIBLE);
                }
            } else {
                if (dateOfMessage != null) {
                    dateOfMessage.setVisibility(View.GONE);
                }
            }
        }

        void bind(QiscusComment comment){
            Glide.with(itemView).load(comment.getSenderAvatar()).into(avatar);

            if (sender != null) {
                sender.setText(comment.getSender());
            }

            date.setText(DateUtil.getTimeStringFromDate(comment.getTime()));
            if (dateOfMessage != null) {
                dateOfMessage.setText(DateUtil.toFullDate(comment.getTime()));
            }

            renderState(comment);
        }
    }

    static class TextVH extends VH{
        private TextView message, sender, dateOfMessage;

        public TextVH( View itemView) {
            super(itemView);
            message = itemView.findViewById(R.id.message);
            sender = itemView.findViewById(R.id.sender);
            dateOfMessage = itemView.findViewById(R.id.dateOfMessage);
        }

        @Override
        void bind(QiscusComment comment) {
            super.bind(comment);
            message.setText(comment.getMessage());
            QiscusChatRoom chatRoom = QiscusCore.getDataStore().getChatRoom(comment.getRoomId());

            if (sender != null && chatRoom != null) {
                if (!chatRoom.isGroup()) {
                    sender.setVisibility(View.GONE);
                } else {
                    sender.setVisibility(View.VISIBLE);
                }
            }

            if (dateOfMessage != null) {
                dateOfMessage.setText(DateUtil.toFullDate(comment.getTime()));
            }
        }

        @Override
        void setNeedToShowDate(Boolean showDate) {
            super.setNeedToShowDate(showDate);
            if (showDate) {
                if (dateOfMessage != null) {
                    dateOfMessage.setVisibility(View.VISIBLE);
                }
            } else {
                if (dateOfMessage != null) {
                    dateOfMessage.setVisibility(View.GONE);
                }
            }
        }
    }
}
