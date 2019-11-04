package com.kafka.user.feature

import android.os.Bundle
import android.view.View
import androidx.fragment.app.commit
import androidx.fragment.app.transaction
import com.kafka.user.R
import com.kafka.user.databinding.FragmentMainBinding
import com.kafka.user.feature.common.DataBindingFragment
import com.kafka.user.feature.home.HomepageFragment
import com.kafka.user.feature.player.PlayerFragment
import com.kafka.user.ui.widget.TabItem
import kotlinx.android.synthetic.main.fragment_main.*

/**
 * @author Vipul Kumar; dated 16/02/19.
 */
class MainFragment : DataBindingFragment<FragmentMainBinding>(
    R.layout.fragment_main
) {
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        launchHomeFragment()

        miniPlayer.setOnClickListener { launchPlayerFragment() }
    }

    private fun launchHomeFragment() {
        childFragmentManager.commit {
            replace(R.id.fragmentContainer, HomepageFragment())
        }
    }

    private fun launchPlayerFragment() {
        childFragmentManager.commit {
            replace(R.id.fragmentContainer, PlayerFragment())
            addToBackStack("")
        }
    }
}
