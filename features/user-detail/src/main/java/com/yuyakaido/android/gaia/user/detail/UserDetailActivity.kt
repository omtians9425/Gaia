package com.yuyakaido.android.gaia.user.detail

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import com.yuyakaido.android.gaia.core.domain.entity.User
import com.yuyakaido.android.gaia.user.detail.databinding.ActivityUserDetailBinding
import dagger.android.support.DaggerAppCompatActivity

class UserDetailActivity : DaggerAppCompatActivity() {

  companion object {
    private val USER = User::class.java.simpleName

    fun createIntent(context: Context, user: User): Intent {
      return Intent(context, UserDetailActivity::class.java)
        .apply { putExtra(USER, user) }
    }
  }

  private val binding by lazy { ActivityUserDetailBinding.inflate(layoutInflater) }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(binding.root)
    setupToolbar()
    setupFragment()
  }

  override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
      android.R.id.home -> finish()
    }
    return super.onOptionsItemSelected(item)
  }

  private fun getUser(): User {
    return requireNotNull(intent.getParcelableExtra(USER))
  }

  private fun setupToolbar() {
    supportActionBar?.title = getString(R.string.user_detail_identity, getUser().name)
    supportActionBar?.setDisplayHomeAsUpEnabled(true)
  }

  private fun setupFragment() {
    val fragment = UserDetailFragment.newInstanceForUser(user = getUser())
    supportFragmentManager
      .beginTransaction()
      .replace(R.id.fragment_container, fragment)
      .commitNow()
  }

}