package tech.astrid.owostick.android

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView

class DeviceAdapter(ctx: Context, private val items: List<DeviceConnector>) : BaseAdapter() {
    private val inflater = LayoutInflater.from(ctx)

    override fun getCount(): Int {
        return items.count()
    }

    override fun getItem(p0: Int): Any {
        return items[p0]
    }

    override fun getItemId(p0: Int): Long {
        return p0.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: inflater.inflate(R.layout.device_list_item, parent, false)
        val vh = (convertView?.tag as DeviceViewHolder?) ?: DeviceViewHolder(view, items[position])
        vh.image.setImageResource(vh.device.drawable)
        vh.title.text = vh.device.name
        view.tag = vh
        return view
    }

    class DeviceViewHolder(private val view: View, val device: DeviceConnector) {
        val image: ImageView = view.findViewById(R.id.deviceTypeIcon)
        val title: TextView = view.findViewById(R.id.deviceName)
    }
}