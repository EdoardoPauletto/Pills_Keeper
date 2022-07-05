package it.uninsubria.cardviewrecyclerview

import android.content.Intent
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import it.uninsubria.cardviewrecyclerview.databinding.ActivityMainBinding
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL


class MainActivity : AppCompatActivity(), BookClickListener {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        populateBooks()

        val mainActivity = this
        binding.recyclerView.apply {
            layoutManager = GridLayoutManager(applicationContext, 2)
            adapter = CardAdapter(bookList, mainActivity)
        }
    }

    @Throws(IOException::class)
    fun drawableFromUrl(url: String?): Drawable? {
        val x: Bitmap
        val connection: HttpURLConnection = URL(url).openConnection() as HttpURLConnection
        connection.connect()
        val input: InputStream = connection.getInputStream()
        x = BitmapFactory.decodeStream(input)
        return BitmapDrawable(Resources.getSystem(), x)
    }

    override fun onClick(book: Book) {
        val intent = Intent(applicationContext, DetailActivity::class.java)
        intent.putExtra(BOOK_ID_EXTRA, book.id)
        startActivity(intent)
    }

    private fun populateBooks() {
        val book1 = Book(
            R.drawable.italy,
            "Victoria Devine",
            "Ageless Body, Timeless Mind",
            "PROVA"
        )

        bookList.add(book1)

        val book2 = Book(
            R.drawable.italy,
            "PROVA",
            "Ageless Body, Timeless Mind",
            "PROVA"
        )

        bookList.add(book2)

        val book3 = Book(
            R.drawable.italy,
            "TEST",
            "TESTT",
            "PROVA"
        )

        bookList.add(book3)

        val book4 = Book(
            R.drawable.italy,
            "Victoria Devine",
            "PROVA1",
            "PROVA"
        )

        bookList.add(book4)
        val book5 = Book(
            R.drawable.italy,
            "Victoria Devine",
            "PROVA2",
            "PROVA"
        )

        bookList.add(book5)
        val book6 = Book(
            R.drawable.italy,
            "Victoria Devine",
            "Ageless Body, Timeless Mind",
            "PROVA"
        )

        bookList.add(book6)
        val book7 = Book(
            R.drawable.italy,
            "Victoria Devine",
            "Ageless Body, Timeless Mind",
            "PROVA"
        )

        bookList.add(book7)
    }


}