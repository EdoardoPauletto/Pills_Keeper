package it.uninsubria.prova

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Adapter
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase

class ListActivity : AppCompatActivity() {
    //val data: MutableList<Upload> = ArrayList()
    //lateinit var adapter: ArrayAdapter<Upload>
    //lateinit var listView: ListView
    lateinit var tv: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list)
        //adapter = ArrayAdapter(this,android.R.layout.simple_list_item_1, data)
        //listView = findViewById<ListView>(R.id.lv_items)
        //listView.adapter = adapter
        tv = findViewById<TextView>(R.id.textView)

        FirebaseDatabase.getInstance().getReference().addChildEventListener(getItemsEventListener())

    }
    private fun getItemsEventListener(): ChildEventListener{
        val childEventListener = object: ChildEventListener{
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                //val elemento = snapshot.getValue(Upload::class.java)
                //data.add(elemento!!)
                //adapter.notifyDataSetChanged()
                tv.text = snapshot.getValue(String::class.java)
            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                //val elemento = snapshot.getValue(Upload::class.java)
                //val posizione = data.indexOf(elemento)
                //data[posizione] = elemento!! //gli assicuro che non sarà null
            }

            override fun onChildRemoved(snapshot: DataSnapshot) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                TODO("Not yet implemented")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        }
        return childEventListener
    }
}