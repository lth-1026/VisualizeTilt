package de.fra_uas.fb2.mobiledevices.visualizetilt

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class GraphFragment : Fragment() {

    private lateinit var glSurfaceView: MyGLSurfaceView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_graph, container, false)
        glSurfaceView = view.findViewById(R.id.glSurfaceView)
        return view
    }
    override fun onResume() {
        super.onResume()
        glSurfaceView.onResume()
    }
    override fun onPause() {
        super.onPause()
        glSurfaceView.onPause()
    }
}