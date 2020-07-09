package com.algolia.instantsearch.showcase

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.algolia.instantsearch.showcase.relateditems.Product
import kotlinx.android.synthetic.main.activity_sample.*
import java.io.Serializable

class SampleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sample)
        val product = extractArg(intent.extras)
        product?.let {
            textView.text = it.name
        }
    }

    companion object {

        private const val PARAM_HIT = "HIT"

        fun getArgs(hit: Product): Bundle {
            return Bundle().apply {
                val arg = SampleActivityArg(hit)
                putSerializable(PARAM_HIT, arg)
            }
        }

        private fun extractArg(bundle: Bundle?): SampleActivityArg? {
            return bundle?.getSerializable(PARAM_HIT) as? SampleActivityArg
        }
    }
}

class SampleActivityArg(
    val id: String? = null,
    val name: String? = null
) : Serializable {

    constructor(product: Product) : this(product.objectID.raw, product.name)
}
