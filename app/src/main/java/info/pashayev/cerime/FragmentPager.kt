package info.pashayev.cerime

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter

class FragmentPager(val mContext: Context, fm: FragmentManager) : FragmentPagerAdapter(fm) {


    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> BalFragment()
            1 -> CerimeFragment()
            else -> BalFragment()
        }
    }

    override fun getCount(): Int {
        return 2
    }


    override fun getPageTitle(position: Int): CharSequence? {

        return when (position) {
            0 -> "Ballar"
            1 -> "Cərimələr"
            else -> null
        }
    }

}