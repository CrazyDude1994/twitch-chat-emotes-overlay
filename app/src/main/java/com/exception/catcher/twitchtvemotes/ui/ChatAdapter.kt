package com.exception.catcher.twitchtvemotes.ui

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.support.v7.widget.RecyclerView
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.exception.catcher.twitchtvemotes.R
import com.exception.catcher.twitchtvemotes.models.EmoteModel

class ChatAdapter : RecyclerView.Adapter<ViewHolder>() {

    private val data: ArrayList<Message> = ArrayList()
    private val emotes: ArrayList<EmoteModel> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, p1: Int): ViewHolder {
        val layoutInfalter = parent.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        return ViewHolder(layoutInfalter.inflate(R.layout.chat_textview, parent, false))
    }

    override fun getItemCount(): Int {
        return data.size
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, p1: Int) {
        val data = data[viewHolder.adapterPosition]
        val spannableString = SpannableString("${data.name}: ${data.message}")
        if (spannableString.indexOf(":") >= 0) {
            spannableString.setSpan(
                ForegroundColorSpan(Color.parseColor(data.color)),
                0,
                spannableString.indexOf(":"),
                0
            )
        }
        (viewHolder.itemView as TextView).text = spannableString
        val offset = "${data.name}: ".length
        data.list.forEach {
            loadEmote(
                "https://static-cdn.jtvnw.net/emoticons/v1/${it.id}/3.0",
                it.start + offset,
                it.end + offset + 1,
                viewHolder
            )
        }
        emotes.let {
            it.forEach {
                val matcher = it.pattern.matcher(viewHolder.itemView.text)
                while (matcher.find()) {
                    val start = matcher.toMatchResult().start()
                    val end = matcher.toMatchResult().end()
                    loadEmote(it.url, start, end, viewHolder)
                }
            }
        }
    }


    fun loadEmote(url: String, start: Int, end: Int, viewHolder: ViewHolder) {
        Glide.with(viewHolder.itemView.context)
            .load(url)
            .into(object : SimpleTarget<Drawable>() {
                override fun onDestroy() {
                    super.onDestroy()
                    Log.d("ChatAdapter", "Destroy")
                }

                override fun onResourceReady(resource: Drawable, transition: Transition<in Drawable>?) {
                    if (resource is Animatable) {
                        resource.start()
                    }
                    resource.setBounds(0, 0, resource.intrinsicWidth / 2, resource.intrinsicHeight / 2)
                    val imageSpan = ImageSpan(resource, ImageSpan.ALIGN_BASELINE)
                    val spannableString = SpannableString((viewHolder.itemView as TextView).text)
                    spannableString.setSpan(imageSpan, start, end, 0)
                    viewHolder.itemView.text = spannableString
                    resource.callback = object : Drawable.Callback {
                        override fun unscheduleDrawable(p0: Drawable, p1: Runnable) {
                            viewHolder.itemView.removeCallbacks(p1)
                        }

                        override fun invalidateDrawable(p0: Drawable) {
                            viewHolder.itemView.invalidate()
                        }

                        override fun scheduleDrawable(p0: Drawable, p1: Runnable, p2: Long) {
                            viewHolder.itemView.postDelayed(p1, p2)
                        }
                    }
                }
            })

    }

    fun addMessage(message: Message) {
        if (data.size > 25) {
            data.removeAt(0)
            notifyItemRangeRemoved(0, 1)
        }
        data.add(message)
        notifyItemInserted(data.size - 1)
    }

    fun addEmotes(emotes: List<EmoteModel>) {
        this.emotes.addAll(emotes)
    }
}

class ViewHolder(item: View) : RecyclerView.ViewHolder(item) {

}

data class Message(var name: String, var message: String, val list: List<TwitchEmote>, val color: String)
data class TwitchEmote(val id: Int, val start: Int, val end: Int)