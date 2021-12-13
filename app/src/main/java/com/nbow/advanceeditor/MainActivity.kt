package com.nbow.advanceeditor

import android.app.Activity
import android.content.*
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ShareCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.testing.FakeReviewManager
import com.nbow.advanceeditor.data.RecentFile
import com.nbow.advanceeditor.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.*
import java.net.URLConnection
import java.util.*
import android.content.Intent
import androidx.core.content.FileProvider
import com.google.android.gms.ads.MobileAds
import android.widget.Toast

import com.google.android.gms.ads.rewarded.RewardItem

import androidx.annotation.NonNull
import androidx.core.view.isVisible
import androidx.transition.Visibility

import com.google.android.gms.ads.OnUserEarnedRewardListener





class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private val TAG = "MainActivity"

    private val THEME_PREFERENCE_KEY = "night_mode_preference"

    val mimeType =
        "text/* |application/java |application/sql |application/php |application/x-php |application/x-javascript |application/javascript |application/x-tcl |application/xml |application/octet-stream"

    val TEXT = "text/*"

    val JAVA = "application/java"
    val SQL = "application/sql"
    val PHP = "application/php"
    val X_PHP = "application/x-php"
    val X_JS = "application/x-javascript"
    val JS = "application/javascript"
    val X_TCL = "application/x-tcl"
    val XML = "application/xml"
    val OCTECT_STRM = "application/octet-stream"


    private var supportedMimeTypes =
        arrayOf(TEXT, JAVA, SQL, PHP, X_PHP, X_JS, JS, X_TCL, XML, OCTECT_STRM)
    private lateinit var toolbar: Toolbar

    //    private lateinit var pager2 : ViewPager2
//    private lateinit var tabLayout : TabLayout
//    private lateinit var fragmentManager: FragmentManager
    private var menu: Menu? = null

    //    private lateinit var bottomNavigationView : BottomNavigationView
    private var darkTheme: Boolean = true

    private lateinit var model: MyViewModel
    private lateinit var adapter: FragmentAdapter
    private lateinit var binding: ActivityMainBinding
    private lateinit var helper: Utils

    private var actionMode: ActionMode? = null
    private lateinit var manager: ReviewManager
    private var index: Int = 0
    private var indexList: MutableList<Int> = arrayListOf()
    private var findText: String = ""
    private var replaceText: String = ""
    private var ignoreCase: Boolean = true
    private lateinit var alertDialogGlobal: AlertDialog
    lateinit var progressBar: ProgressBar
    lateinit var constraintLayout: ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e(TAG, "onCreate: callled")
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        if(savedInstanceState==null)
            askForRating()
        if (intent != null && savedInstanceState == null) {
            val uri: Uri? = intent.data
            if (uri !== null) {
                readFileUsingUri(uri,true)
            }
        }
        MobileAds.initialize(this) {}
        Admob.loadAd(applicationContext)

    }


    private fun init() {


        model =
            ViewModelProvider(this, MyViewModelFactory(this.application)).get(
                MyViewModel::class.java
            )
        helper  = Utils(this)
        if (!helper.isStoragePermissionGranted()) helper.takePermission()

        toolbar = findViewById(R.id.toolbar)
        adapter = FragmentAdapter(fragmentManager = supportFragmentManager, lifecycle = lifecycle)
        adapter.fragmentList = arrayListOf()
        binding.pager2.adapter = adapter
        binding.pager2.isUserInputEnabled = false
        val v:View  = binding.noTabLayout.cl1
        v.setOnClickListener({
            try {
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                    putExtra(Intent.EXTRA_TITLE, "new.txt")
                }
                newFileLauncher.launch(intent)
            }
            catch (e:Exception)
            {
                Toast.makeText(applicationContext, "${e.message.toString()}", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "newFileLauncher: ${e.toString()}.")
            }

        })
        val v2:View = binding.noTabLayout.cl2
        v2.setOnClickListener({
            if (!helper.isStoragePermissionGranted()) helper.takePermission()

            if (helper.isStoragePermissionGranted()) chooseFile()

        })
        manager = ReviewManagerFactory.create(applicationContext)


        setDefaultToolbarTitle()
        toolbar.apply {
            setNavigationIcon(R.drawable.ic_navigation)
        }

        setSupportActionBar(toolbar)
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                changeNoTabLayout()
                binding.pager2.setCurrentItem(tab!!.position, true)
                Log.e(TAG, "onTabSelected: position ${tab.position}")
                toolbar.apply {
                    if (isValidTab()) {
                        (adapter.fragmentList.get(tab.position) as EditorFragment).apply {
                            title = getFileName()
                            subtitle = ""
                        }

                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}

        })


        binding.bottamBarLayout!!.apply {

            var currentFragment: EditorFragment? = null

            if (isValidTab()) {
                currentFragment = adapter.fragmentList.get(binding.tabLayout.selectedTabPosition) as EditorFragment
            }

            close.setOnClickListener {
                currentFragment = adapter.fragmentList.get(binding.tabLayout.selectedTabPosition) as EditorFragment

                if (currentFragment != null) {
                    //if (currentFragment!!.hasUnsavedChanges.value ?: false) {
                      //  showUnsavedDialog(currentFragment!!)
                    //} else {
                        closeTab()
                    //}
                }
            }

            open.setOnClickListener {

                if (!helper.isStoragePermissionGranted()) helper.takePermission()
                if (helper.isStoragePermissionGranted()) chooseFile()
            }

            save.setOnClickListener {
                currentFragment = adapter.fragmentList.get(binding.tabLayout.selectedTabPosition) as EditorFragment

                if (currentFragment != null) {

                    //if (currentFragment!!.hasUnsavedChanges.value != false) {
                        saveFile(currentFragment!!, currentFragment!!.getUri())
                    //} else
//                        Toast.makeText(
//                            this@MainActivity,
//                            "No Changes Found",
//                            Toast.LENGTH_SHORT
//                        ).show()
                }
            }
            search.setOnClickListener {
                currentFragment = adapter.fragmentList.get(binding.tabLayout.selectedTabPosition) as EditorFragment

                if (currentFragment != null)
                    search(currentFragment!!, false)
            }

            toolbox.setOnClickListener {
                binding.specialCharLayout.specialCharKeyboarLayout.apply {
                    if (visibility == View.VISIBLE) visibility = View.GONE
                    else visibility = View.VISIBLE
                }
            }

            undoChange.setOnClickListener {
                currentFragment = adapter.fragmentList.get(binding.tabLayout.selectedTabPosition) as EditorFragment

                if (currentFragment != null) {
                    currentFragment!!.undoChanges()
                }


            }
            redoChange.setOnClickListener {
                currentFragment = adapter.fragmentList.get(binding.tabLayout.selectedTabPosition) as EditorFragment

                if (currentFragment != null) {
                    currentFragment!!.redoChanges()
                }
            }

        }






        binding.contextualBottomNavigation.setOnItemSelectedListener(object :
            NavigationBarView.OnItemSelectedListener {
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                var currentFragment: EditorFragment? = null

                if (isValidTab()) {
                    currentFragment =
                        adapter.fragmentList.get(binding.tabLayout.selectedTabPosition) as EditorFragment
                }

                if (currentFragment != null)
                    return when (item.itemId) {

                        R.id.up -> {
                            if (indexList.isNotEmpty()) {
                                index = currentFragment!!.highlight(
                                    findText,
                                    indexList.last(),
                                    true
                                )//TODO : remaining
                                indexList.remove(indexList.last())
                            }
//                            Toast.makeText(this@MainActivity, "up", Toast.LENGTH_SHORT).show()
                            true
                        }
                        R.id.down -> {
                            down(currentFragment!!)
//                            Toast.makeText(this@MainActivity, "down", Toast.LENGTH_SHORT).show()
                            true
                        }
                        R.id.replace -> {
                            index = currentFragment!!.findReplace(
                                findText,
                                replaceText,
                                index,
                                ignoreCase
                            )
                            down(currentFragment!!)
                            if (index == -1) {
                                Toast.makeText(
                                    this@MainActivity,
                                    "search not found",
                                    Toast.LENGTH_SHORT
                                ).show()
                                if (actionMode != null)
                                    actionMode!!.finish()
                            }

//                            Toast.makeText(this@MainActivity, "replace", Toast.LENGTH_SHORT).show()
                            true
                        }
                        R.id.replace_all -> {

                            currentFragment!!.replaceAll(findText, replaceText, ignoreCase)
                            if (actionMode != null)
                                actionMode!!.finish()
//                            Toast.makeText(this@MainActivity, "replace all", Toast.LENGTH_SHORT).show()
                            true
                        }
                        else -> false
                    }

                return false
            }

        })


        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)



        binding.specialCharLayout.charToolbar.setOnMenuItemClickListener({
            var currentFragment: EditorFragment? = null

            if (isValidTab()) {
                currentFragment =
                    adapter.fragmentList.get(binding.tabLayout.selectedTabPosition) as EditorFragment
            }

            if (currentFragment != null)
                when (it.itemId) {
                    R.id.tab -> {
                        currentFragment!!.insertSpecialChar("    ")
                    }
                    R.id.roundOpen -> {
                        currentFragment!!.insertSpecialChar("()")
                        currentFragment!!.selectionPrevPosition()

                    }
                    R.id.roundClose -> {
                        currentFragment!!.insertSpecialChar(")")
                    }
                    R.id.breaketOpen -> {
                        currentFragment!!.insertSpecialChar("[]")
                        currentFragment!!.selectionPrevPosition()
                    }
                    R.id.breaketClose -> {
                        currentFragment!!.insertSpecialChar("]")
                    }
                    R.id.curlyOpen -> {
                        currentFragment!!.insertSpecialChar("{}")
                        currentFragment!!.selectionPrevPosition()

                    }
                    R.id.curlyClose -> {
                        currentFragment!!.insertSpecialChar("}")
                    }
                    R.id.empercent -> {
                        currentFragment!!.insertSpecialChar("&")
                    }
                    R.id.singlequote -> {
                        currentFragment!!.insertSpecialChar("\'")
                    }
                    R.id.doublequote -> {
                        currentFragment!!.insertSpecialChar("\"")
                    }
                    R.id.dollor -> {
                        currentFragment!!.insertSpecialChar("$")
                    }
                    R.id.greaterThan -> {
                        currentFragment!!.insertSpecialChar(">")
                    }
                    R.id.lessThan -> {
                        currentFragment!!.insertSpecialChar("<")
                    }

                }
            false
        })

    }

    //sdfklsdflks;dlfk;sdkf;sldkf;ladskf;ldsf
    //sadflsdkf;sdkf;lsad
    //why call every timr
    // asdfasd
    //dsfsadf
    //dsafsdfsdaf
    //dsdsfsadf
    ///sdfasdfasdfsa
    ////fasdfasdfs
    //fsadfasdfd
    private fun changeNoTabLayout() {
        binding.apply {
            if(tabLayout.tabCount==0)
            {
                noTabLayout.root.visibility=View.VISIBLE
                constraintLayoutMain.visibility = View.GONE
            }
            else
            {
                (noTabLayout.root).visibility = View.GONE
                constraintLayoutMain.visibility = View.VISIBLE
            }
        }
    }

    private fun showUnsavedDialog(currentFragment: EditorFragment) {
        val builder = AlertDialog.Builder(this)

        builder.setTitle("Unsaved File")
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        val view = LayoutInflater.from(this).inflate(R.layout.unsaved_dialog, null, false)


        builder.setView(view)

        builder.setPositiveButton("Yes") { dialogInterface, which ->
            run {
                saveFile(currentFragment, currentFragment.getUri(), false, true)
                dialogInterface.dismiss()
            }
        }
        //performing cancel action
        builder.setNeutralButton("Cancel") { dialogInterface, which ->
            //Toast.makeText(applicationContext, "operation cancel", Toast.LENGTH_LONG).show()
            dialogInterface.dismiss()
        }

        builder.setNegativeButton("No") { dialogInterface, which ->
            run {
                closeTab()
                dialogInterface.dismiss()
            }
        }

        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(true)
        alertDialog.show()


    }

    val recyclerViewLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent: Intent? = result.data
                val uri: Uri? = Uri.parse(intent?.getStringExtra("uri"))
                if (uri != null) readFileUsingUri(uri)
            }
        }
    val AdViewLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {

            }
        }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        Log.e(TAG, "onNavigationItemSelected: outside ")
        when (item.itemId) {
            R.id.nav_history -> {
                // Handle the camera action
                var intent: Intent = Intent(this, RecyclerViewActivity::class.java)
                recyclerViewLauncher.launch(intent)
            }
            R.id.nav_setting -> {
                Log.e(TAG, "onNavigationItemSelected: clicked")
                val intent = Intent(this@MainActivity, SettingActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_storage_manager -> {
                if (!helper.isStoragePermissionGranted()) helper.takePermission()

                if (helper.isStoragePermissionGranted()) chooseFile()
            }
            R.id.nav_feedback -> {
                feedback()
            }
            R.id.nav_ad -> {

                if (Admob.rewardedAd != null) {
                    Log.e(TAG, "onNavigationItemSelected: admob initialized", )
                    Admob.rewardedAd!!.show(
                        this,
                        OnUserEarnedRewardListener { rewardItem ->
                            Toast.makeText(
                                this@MainActivity,
                                "Rewarded with Thanks", Toast.LENGTH_SHORT
                            ).show()
                        })
                }

                Admob.loadAd(applicationContext)
            }

        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }


    private fun down(currentFragment: EditorFragment) {
        if (index != -1) {
            indexList.add(index)
            index = currentFragment.highlight(findText, index + 1, ignoreCase)
        }
        if (index == -1) {
            indexList.clear()
            index = 0
            index = currentFragment.highlight(findText, index, ignoreCase)
        }
    }


    private fun closeTab() {
        binding.tabLayout.apply {
            if (isValidTab()) {
                adapter.apply {
                    fragmentList.removeAt(selectedTabPosition)
                    notifyItemRemoved(selectedTabPosition)
                }
                removeTabAt(selectedTabPosition)
                if (tabCount == 0) {
                    setDefaultToolbarTitle()
                }
            }
        }

    }

    private fun setDefaultToolbarTitle() {
        toolbar.apply {
            setTitle(R.string.greet_line)
            changeNoTabLayout()
        }
    }

    private fun isValidTab(): Boolean {
        binding.tabLayout.apply {
            if (tabCount > 0 && selectedTabPosition >= 0 && selectedTabPosition < adapter.itemCount)
                return true
            return false
        }
    }

    override fun onResume() {
        super.onResume()

        //lifecycleScope.launch(Dispatchers.Main){

        val preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)
        val wrap = preferences.getBoolean("word_wrap", false)

        lifecycleScope.launch(Dispatchers.IO)
        {
            Utils.isLineNumber = preferences.getBoolean("line_number", true)
        }

            //val keyIsThemeChanged = "is_theme_changed_setting"
            // val isThemeChangedFromSetting = preferences.getBoolean(keyIsThemeChanged, false)
            if (wrap != model.isWrap) {
                model.isWrap = wrap
                recreate()
            }
            //darkTheme = preferences.getBoolean(THEME_PREFERENCE_KEY, true)
            //changeTheme()

            model.isHistoryLoaded.observe(this@MainActivity) {
                adapter.fragmentList = model.getFragmentList().value ?: arrayListOf()
                binding.tabLayout.apply {
                    if (tabCount > 0) {
                        Log.e(TAG, "onResume: selectedtabposition $selectedTabPosition")
                        model.currentTab = selectedTabPosition
                    }
                    else {
                        makeBlankFragment("untitled")
                    }
                }
                createTabsInTabLayout(adapter.fragmentList)
            }

//            if (binding.tabLayout.tabCount > 0 && isThemeChangedFromSetting) {
//                model.currentTab = 0
//                val editor = preferences.edit()
//                //editor.putBoolean(keyIsThemeChanged, false)
//                editor.apply()
//            }


            binding.tabLayout.apply {
                if (model.currentTab >= 0 && model.currentTab < tabCount)
                    selectTab(getTabAt(model.currentTab))
            }
            Log.e(TAG, "onResume: called")


            for ((count, frag) in model.getFragmentList().value!!.withIndex()) {
                val fragment = frag as EditorFragment
                fragment.hasUnsavedChanges.observe(this@MainActivity) {
                    if (it){
                        setCustomTabLayout(count, "*${fragment.getFileName()}")
                    }
                }
                fragment.hasLongPress.observe(this@MainActivity){
                    if(it) {
                        startActionMode(actionModeCallbackCopyPaste)
                        fragment.hasLongPress.value = false
                    }


                }
            }
        //progressBar.visibility=View.VISIBLE
      //  }

    }

    fun makeBlankFragment(fileName: String)
    {

        Log.e(TAG, "makeBlankFragment: " )
        val list:MutableList<StringBuilder> = arrayListOf()
        val dataFile = DataFile(
            fileName = fileName,
            filePath = "note",
            uri = Uri.parse(""),
            list
        )
        val fragment = EditorFragment(dataFile,hasUnsavedChanges = true)
        adapter.addFragment(fragment)

        binding.tabLayout.apply {
            this.addTab(newTab())
            setCustomTabLayout(tabCount - 1, fileName)
            adapter.notifyItemInserted(tabCount - 1)
            selectTab(getTabAt(tabCount - 1))
        }
        fragment.hasUnsavedChanges.observe(this@MainActivity) {
            if (it) {
                setCustomTabLayout(binding.tabLayout.tabCount-1, "*${fragment.getFileName()}")
            }else setCustomTabLayout(binding.tabLayout.tabCount-1, fragment.getFileName())
        }
    }

    private fun changeTheme() {

        if (darkTheme) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }


    override fun onStop() {
        super.onStop()
        Log.e(TAG, "onStop: called")

        if (isValidTab()) {
            model.currentTab = binding.tabLayout.selectedTabPosition
        }

        for (frag in adapter.fragmentList) {
            val fragment = frag as EditorFragment
            fragment.saveDataToPage()
        }
        model.addHistories(applicationContext)
    }

    override fun onDestroy() {

        // saving all files to databse
        model.setFragmentList(adapter.fragmentList)

        // saving current tab
        if (adapter.fragmentList.size > 0)
            model.currentTab = binding.pager2.currentItem
        super.onDestroy()
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {


        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.open -> showPopupMenu(item, R.menu.open_file_menu)
            R.id.edit -> showPopupMenu(item, R.menu.edit_menu)
            R.id.overflow_menu -> showPopupMenu(item, R.menu.overflow_menu)
        }

        return super.onOptionsItemSelected(item)
    }

    private fun showPopupMenu(item: MenuItem, menuResourceId: Int) {
        val view = findViewById<View>(item.itemId)
        val popup = PopupMenu(this, view)

        popup.inflate(menuResourceId)

        val preference = PreferenceManager.getDefaultSharedPreferences(this)
        val isWrap = preference.getBoolean("word_wrap", false)

        val item = popup.menu.findItem(R.id.go_to_line)
        if (item != null) {
            item.setVisible(!isWrap)
        }
        popup.setOnMenuItemClickListener { item -> //TODO : list all action for menu popup
//            Log.e(TAG, "onMenuItemClick: " + item.title)
            var currentFragment: EditorFragment? = null

            if (isValidTab()) {
                currentFragment =
                    adapter.fragmentList[binding.tabLayout.selectedTabPosition] as EditorFragment
            }

            when (item.itemId) {

                R.id.open -> {

                    if (!helper.isStoragePermissionGranted()) helper.takePermission()

                    if (helper.isStoragePermissionGranted()) chooseFile()
//                    Log.e(TAG, "showPopupMenu: open called")
                }
                R.id.save_as -> {
                    saveAsIntent(currentFragment)
                }
                R.id.save -> {
                    if (currentFragment != null) {
                        if (currentFragment.hasUnsavedChanges.value != false)
                            saveFile(currentFragment, currentFragment.getUri())
                        else
                            Toast.makeText(this, "No Changes Found", Toast.LENGTH_SHORT).show()
                    }
                }
                R.id.close -> {
                    if (currentFragment != null) {
                        if (currentFragment.hasUnsavedChanges.value ?: false) {
                            showUnsavedDialog(currentFragment)
                        } else {
                            closeTab()
                        }
                    }
                }
                R.id.new_file -> {
                    //TODO : remaining ....
                    try {


                        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                            addCategory(Intent.CATEGORY_OPENABLE)
                            type = "*/*"
                            putExtra(Intent.EXTRA_TITLE, "new.txt")
                        }
                        newFileLauncher.launch(intent)
                    }
                    catch (e:Exception)
                    {
                        Toast.makeText(applicationContext, "${e.message.toString()}", Toast.LENGTH_SHORT).show()
                        Log.e(TAG, "newFileLauncher: ${e.toString()}.")
                    }
                }
                R.id.paste -> {
                    val clipboardManager =
                        getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val dataToPaste = clipboardManager.primaryClip?.getItemAt(0)?.text
                    if (currentFragment !== null) {
                        currentFragment.insertSpecialChar(dataToPaste.toString())
                    }

                }

                R.id.reload -> {
                    if (currentFragment != null)
                        reloadFile(currentFragment)
                }

                R.id.copy -> {
                    if (currentFragment !== null) {
                        val selectedData = currentFragment.getSelectedData()
                        if (selectedData != null) copy(selectedData)
                    }

                }
                R.id.select_all -> {
                    //TODO : remaining ...
                    if (currentFragment != null) {
                        currentFragment.selectAll()
                        actionMode=startActionMode(actionModeCallbackCopyPaste)
                    }
                }
                R.id.go_to_line -> {
                    //TODO : remaining ...
                    gotoLine()
                }
                R.id.search -> {
                    if (currentFragment != null)
                        search(currentFragment, false)

                }
                R.id.search_replace -> {
                    if (currentFragment != null)
                        search(currentFragment, true)
                }
                R.id.run->{
                    val intent:Intent = Intent(this,WebViewActivity::class.java)
                    if(currentFragment!=null) {
                        intent.putExtra("data", currentFragment.getEditTextData().toString())
                        startActivity(intent)
                    }

                }
                R.id.change_editor_theme -> {
                    if (currentFragment != null)
                    {
                        currentFragment.changeCodeViewTheme()
                    }

                }
//                R.id.mini_toolbar -> {
//
//                    if(binding.bottomNavigation.isVisible)
//                    {
//                        binding.bottomNavigation.visibility= View.GONE
//                        binding.specialCharLayout.root.visibility = View.GONE
//
//                    }
//                    else
//                    {
//                        binding.bottomNavigation.visibility= View.VISIBLE
//                        binding.specialCharLayout.root.visibility = View.VISIBLE
//
//
//                    }
//                }

                R.id.texteditor -> {

                    try{
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.nbow.texteditor")))
                    }
                    catch (e:Exception)
                    {
                        Toast.makeText(applicationContext, "Hello", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.nbow.texteditor")))
                    }
                }
                R.id.settings -> {
                    Log.e(TAG, "onNavigationItemSelected: clicked")
                    val intent: Intent = Intent(this@MainActivity, SettingActivity::class.java)
                    startActivity(intent)
                }
                R.id.share -> {
                    if (currentFragment != null ) {

                        val prefix= currentFragment.getFileName().substringBeforeLast('.')
                        val suffix=currentFragment.getFileExtension()
                        val file = File.createTempFile(prefix,suffix,applicationContext.cacheDir)

                        file.bufferedWriter().use {

                                it.write(currentFragment.getEditTextData().toString())
                        }

                        ShareCompat.IntentBuilder(this)
                            .setStream(FileProvider.getUriForFile(applicationContext,BuildConfig.APPLICATION_ID+".provider",file))
                            .setType(URLConnection.guessContentTypeFromName(currentFragment.getFileName()))
                            .startChooser()

                    }


                }
                R.id.undo_change -> {
                    if(currentFragment!=null)
                    {
                        currentFragment.undoChanges()
                        actionMode = startActionMode(actionModeCallbackUndoRedo)
                    }
                }
                R.id.redo_change->{

                    if(currentFragment!=null)
                    {
                        currentFragment.redoChanges()
                        actionMode = startActionMode(actionModeCallbackUndoRedo)
                    }

                }


            }
            false
        }
        val menuHelper: Any
        val argTypes: Array<Class<*>?>
        try {
            val fMenuHelper = PopupMenu::class.java.getDeclaredField("mPopup")
            fMenuHelper.isAccessible = true
            menuHelper = fMenuHelper[popup]
            argTypes = arrayOf(Boolean::class.javaPrimitiveType)
            menuHelper.javaClass.getDeclaredMethod("setForceShowIcon", *argTypes)
                .invoke(menuHelper, true)
        } catch (e: Exception) {
        }
        popup.show()
    }

    private fun reloadFile(currentFragment: EditorFragment) {
            val uri = currentFragment.getUri()
            if(uri!=null)
                readFileUsingUri(uri,false,true)

    }

    val resLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent: Intent? = result.data
                var uri = intent?.data

                if (uri !== null) readFileUsingUri(uri)

            }
        }


    private fun readFileUsingUri(uri: Uri,isOuterFile : Boolean = false,isReload : Boolean = false) {

        try {
            val takeFlags: Int =
                Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                applicationContext.contentResolver.takePersistableUriPermission(uri, takeFlags)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val fileSize: Int = inputStream!!.available()
            val bufferedReader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
            val listOfLines: MutableList<String> = arrayListOf()
            val listOfPageData: MutableList<java.lang.StringBuilder> = arrayListOf()

            bufferedReader.forEachLine {
                listOfLines.add(it)
            }

            val temp = StringBuilder("")
            var count = 0
            var index=0
            while(index<listOfLines.size){
                temp.append(listOfLines[index])
                count++
                if (count >= 1000 || temp.length >= 50000) { // 500kb
//                Log.e(TAG, "readFileUsingUri: temp : at $count : $temp")
                    listOfPageData.add(temp)
                    temp.clear()
                    count = 0
                } else if(index!=listOfLines.size-1) temp.append("\n")

                index++
            }
//            for (line in listOfLines) {
//                temp.append(line)
//                count++
//                if (count >= 3000 || temp.length >= 500000) { // 500kb
////                Log.e(TAG, "readFileUsingUri: temp : at $count : $temp")
//                    listOfPageData.add(temp)
//                    temp.clear()
//                    count = 0
//                } else temp.append("\n")
//
//            }
            if (temp.length > 0) {
                listOfPageData.add(temp)
            }
            if (listOfLines.size == 0) {
                listOfPageData.add(temp)
            }

            val fileName: String = helper.queryName(contentResolver, uri)
            val dataFile = DataFile(
                fileName = fileName,
                filePath = uri.path!!,
                uri = uri,
                listOfPageData = listOfPageData
            )
            val fragment = EditorFragment(dataFile)

            if (isReload && isValidTab()) {
                val position = binding.tabLayout.selectedTabPosition
                adapter.fragmentList.removeAt(position)
                adapter.fragmentList.add(position, fragment)
                setCustomTabLayout(position, "$fileName")
                adapter.notifyDataSetChanged()

            } else {
                adapter.addFragment(fragment)
                binding.tabLayout.apply {
                    addTab(newTab())
                    setCustomTabLayout(tabCount - 1, fileName)
                    adapter.notifyItemInserted(tabCount - 1)
                    selectTab(getTabAt(tabCount - 1))
                    if (isOuterFile) {
                        model.getFragmentList().value?.add(fragment)
                        model.currentTab = tabCount - 1
                    }
                }

                model.addRecentFile(
                    RecentFile(
                        0,
                        uri.toString(),
                        fileName,
                        Calendar.getInstance().time.toString(),
                        fileSize
                    )
                )
            }
            fragment.hasUnsavedChanges.observe(this) {
                if (it)
                    setCustomTabLayout(binding.tabLayout.selectedTabPosition, "*$fileName")
                else setCustomTabLayout(binding.tabLayout.selectedTabPosition, "$fileName")
            }
            fragment.hasLongPress.observe(this@MainActivity){
                if(it) {
                    startActionMode(actionModeCallbackCopyPaste)
                    fragment.hasLongPress.value = false
                }
            }
            Log.e(
                TAG,
                "readFileUsingUri : tab layout selected position : ${binding.tabLayout.selectedTabPosition}"
            )
        }
        catch (e:Exception)
        {
            Toast.makeText(applicationContext,"${e.message.toString()}",Toast.LENGTH_SHORT).show()
        }

    }


    private fun chooseFile() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            val intent: Intent = Intent(Intent.ACTION_OPEN_DOCUMENT).setType("*/*")
            intent.putExtra(Intent.EXTRA_MIME_TYPES, supportedMimeTypes)
            resLauncher.launch(intent)
        } else {
            val intent: Intent = Intent(Intent.ACTION_GET_CONTENT).setType(mimeType)
            resLauncher.launch(intent)
        }
    }

    private fun createTabsInTabLayout(list: MutableList<Fragment>) {
//        Log.e(TAG, "createTabsInTabLayout: called "+list.size)
        // salkdlaskdlasdlsakd;lask sa;dk;aslkd;aslkd;aslkd;aslkd;als
        binding.tabLayout.removeAllTabs()

        if (binding.tabLayout.tabCount == 0) {

//                Log.e(TAG, "createTabsInTabLayout: $it")
            binding.tabLayout.apply {
                list.forEach {
                    //                  Log.e(TAG, "createTabsInTabLayout: tab count inside apply $tabCount")
                    val frag = it as EditorFragment
                    addTab(newTab())
                    setCustomTabLayout(tabCount - 1, frag.getFileName())
                }

                binding.pager2.currentItem = selectedTabPosition
            }

        }
        if (model.currentTab >= 0 && model.currentTab < adapter.fragmentList.size) {
            binding.pager2.setCurrentItem(model.currentTab)
        }
        adapter.notifyDataSetChanged()
    }

    private fun setCustomTabLayout(position: Int, fileName: String) {
        binding.tabLayout.apply {
            if (position >= 0 && position < tabCount) {
                val tab = getTabAt(position)
                tab?.apply {
                    if (customView == null) {
                        setCustomView(R.layout.tab_layout)
                    }
                    customView!!.findViewById<TextView>(R.id.file_name).setText(fileName)
                }
            }
        }
    }

    val saveAsSystemPickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent: Intent? = result.data
                val uri: Uri? = intent?.data
                if (uri != null) {
//                    Log.e(TAG, "save as sytem picker: uri -> $uri")
                    if (isValidTab()) {
                        val fragment =
                            adapter.fragmentList.get(binding.tabLayout.selectedTabPosition) as EditorFragment
                        saveFile(fragment, uri, isSaveAs = true)
                    }
                }
            }
        }

    val newFileLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent: Intent? = result.data
                val uri: Uri? = intent?.data
                if (uri != null) readFileUsingUri(uri)
            }
        }

    private fun saveFile(
        fragment: EditorFragment,
        uri: Uri?,
        isSaveAs: Boolean = false,
        isCloseFlag: Boolean = false
    ) {

//        val uri = fragment.getUri()
        if (uri !== null) {
            try {
                val takeFlags: Int =
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                    applicationContext.contentResolver.takePersistableUriPermission(uri, takeFlags)
                contentResolver.openFileDescriptor(uri, "wt")?.use {
                    FileOutputStream(it.fileDescriptor).use {
                        it.write(
                            fragment.getEditTextData().toString().toByteArray()
                        )
                        if (!isSaveAs)
                            fragment.hasUnsavedChanges.value = false
                        if (isValidTab()) setCustomTabLayout(
                            binding.tabLayout.selectedTabPosition,
                            fragment.getFileName()
                        )
//                        Toast.makeText(applicationContext, "File Saved", Toast.LENGTH_SHORT).show()

                        showProgressBarDialog("Saved Successfully", isCloseFlag)
                    }
                }
            } catch (e: FileNotFoundException) {
                Toast.makeText(applicationContext, "File Doesn't Saved", Toast.LENGTH_SHORT).show()
                e.printStackTrace()

            } catch (e: IOException) {
                Toast.makeText(applicationContext, "File Doesn't Saved", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            } catch (e: SecurityException) {
                showSecureSaveAsDialog(fragment)
            } catch (e: Exception) {
                Toast.makeText(applicationContext, "File Doesn't Saved", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    private fun showSecureSaveAsDialog(fragment: EditorFragment) {
        val builder = AlertDialog.Builder(this)

        builder.setTitle("Security Alert")
        builder.setIcon(android.R.drawable.ic_dialog_alert)

        val view = LayoutInflater.from(this).inflate(R.layout.security_alert_dialog, null, false)


        builder.setView(view)

        builder.setPositiveButton(this.getString(R.string.save_as)) { dialogInterface, which ->
            run {
                saveAsIntent(fragment)
                dialogInterface.dismiss()
            }
        }
        //performing cancel action
        builder.setNeutralButton(this.getString(R.string.cancel)) { dialogInterface, which ->
            dialogInterface.dismiss()
        }

        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(true)
        alertDialog.show()


    }

    private fun saveAsIntent(currentFragment: EditorFragment?) {
        if (currentFragment != null) {

            try {
                if (currentFragment != null) {

                    val fileExtension = currentFragment.getFileExtension()
                    val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "*/*"
                        putExtra(Intent.EXTRA_TITLE, "untitled${fileExtension}")
                    }
                    saveAsSystemPickerLauncher.launch(intent)
                }
            }
            catch (e:Exception)
            {
                Toast.makeText(applicationContext, "${e.message.toString()}", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "saveAsIntent: ${e.toString()}.")
            }
        }
    }


    private fun showProgressBarDialog(title: String, isCloseFlag: Boolean = false) {
        val builder = AlertDialog.Builder(this)

        val view = LayoutInflater.from(this).inflate(R.layout.save_successfull, null, false)
        val titleText = view.findViewById<TextView>(R.id.dialog_title)
        titleText.setText(title)
        builder.setView(view)

//        builder.setPositiveButton("Done"){ dialogInterface, which -> dialogInterface.dismiss() }

        // Create the AlertDialog
        alertDialogGlobal = builder.create()
        // Set other dialog properties
        alertDialogGlobal.setCancelable(true)
        alertDialogGlobal.show()

        lifecycleScope.launch(Dispatchers.Main) {
            delay(400)
            alertDialogGlobal.dismiss()
            if (isCloseFlag) {
                closeTab()
            }
        }

    }


    private fun search(currentFragment: EditorFragment, hasReplace: Boolean) {
        val builder = AlertDialog.Builder(this)

        builder.setTitle("Search")
        builder.setIcon(R.drawable.ic_search)

        val view = LayoutInflater.from(this).inflate(R.layout.search_dialog, null, false)
        val findEditText = view.findViewById<EditText>(R.id.search_text)
        val replaceEditText = view.findViewById<EditText>(R.id.replace_text)
        val ignoreCaseCheckBox = view.findViewById<CheckBox>(R.id.ignore_case)

        if (hasReplace) {
            replaceEditText.visibility = View.VISIBLE
            builder.setTitle("Search And Replace")
        } else {
            replaceEditText.visibility = View.GONE
        }

        builder.setView(view)

        builder.setPositiveButton("Find") { dialogInterface, which ->
            run {
                findText = findEditText.text.toString()
                replaceText = replaceEditText.text.toString()
                ignoreCase = ignoreCaseCheckBox.isChecked


                actionMode = startActionMode(actionModeCallback)

                actionMode.apply {
                    if(hasReplace)
                        this?.title=" "+findText+" --> "+replaceText
                    else
                        this?.title = "Search : "+findText
                }
                binding.specialCharLayout.root.visibility = View.GONE

                if (!hasReplace)
                    binding.contextualBottomNavigation.apply {
                        this.menu.findItem(R.id.replace).setVisible(false)
                        this.menu.findItem(R.id.replace_all).setVisible(false)
                    }


                index = currentFragment.highlight(findEditText.text.toString(), 0, ignoreCase)
                Log.e(TAG, "search: index : $index")
                dialogInterface.dismiss()
            }
        }
        //performing cancel action
        builder.setNeutralButton("Cancel") { dialogInterface, which ->
            //Toast.makeText(applicationContext, "operation cancel", Toast.LENGTH_LONG).show()
            dialogInterface.dismiss()
        }

        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(true)
        alertDialog.show()
        if (TextUtils.isEmpty(findEditText.text)) {
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
        }
        findEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = !TextUtils.isEmpty(s)
            }
        })

    }

    private val actionModeCallback = object : ActionMode.Callback {
        // Called when the action mode is created; startActionMode() was called
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            // Inflate a menu resource providing context menu items
//            val inflater: MenuInflater = mode.menuInflater
//            inflater.inflate(R.menu.context_menu, menu)

            binding.contextualBottomNavigation.apply {
                visibility = View.VISIBLE
                this.menu.findItem(R.id.replace).setVisible(true)
                this.menu.findItem(R.id.replace_all).setVisible(true)
            }

            return true
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.setTitle("Search And Replace")
            return false // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
            return when (item.itemId) {
//                R.id.up -> {
//                    Toast.makeText(this@MainActivity, "up", Toast.LENGTH_SHORT).show()
////                    mode.finish() // Action picked, so close the CAB
//                    true
//                }

                else -> false
            }
        }

        // Called when the user exits the action mode
        override fun onDestroyActionMode(mode: ActionMode) {
            binding.contextualBottomNavigation.visibility = View.GONE
            actionMode = null
        }
    }

    private val actionModeCallbackUndoRedo = object : ActionMode.Callback {




        // Called when the action mode is created; startActionMode() was called
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            // Inflate a menu resource providing context menu items
            val inflater: MenuInflater = mode.menuInflater
            inflater.inflate(R.menu.undo_redo_menu, menu)

            return true
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.setTitle("Undo & Redo")
            return false // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {


            var currentFragment: EditorFragment? = null

            if (isValidTab()) {
                currentFragment =
                    adapter.fragmentList.get(binding.tabLayout.selectedTabPosition) as EditorFragment
            }

            return when (item.itemId) {

                // actioMode
                R.id.undo_change -> {
                    if(currentFragment!=null)
                    {
                        currentFragment.undoChanges()

                    }
                    true
                }
                // actioMode
                R.id.redo_change->{

                    if(currentFragment!=null)
                    {
                        currentFragment.redoChanges()

                    }
                    true
                }
                else -> false
            }
        }

        // Called when the user exits the action mode
        override fun onDestroyActionMode(mode: ActionMode) {
            binding.contextualBottomNavigation.visibility = View.GONE
            actionMode = null
        }
    }


    private val actionModeCallbackCopyPaste = object : ActionMode.Callback {




        // Called when the action mode is created; startActionMode() was called
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            // Inflate a menu resource providing context menu items
            val inflater: MenuInflater = mode.menuInflater
            inflater.inflate(R.menu.copy_paste_menu, menu)

            return true
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
            mode.setTitle("Copy & Paste")
            return false // Return false if nothing is done
        }

        // Called when the user selects a contextual menu item
        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {


            var currentFragment: EditorFragment? = null

            if (isValidTab()) {
                currentFragment =
                    adapter.fragmentList.get(binding.tabLayout.selectedTabPosition) as EditorFragment
            }

            return when (item.itemId) {

                    // actioMode
                R.id.paste -> {
                    val clipboardManager =
                        getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val dataToPaste = clipboardManager.primaryClip?.getItemAt(0)?.text
                    if (currentFragment !== null) {
                        currentFragment.insertSpecialChar(dataToPaste.toString())
                    }
                    true
                }

                // actioMode
                R.id.copy -> {
                    if (currentFragment !== null) {
                        val selectedData = currentFragment.getSelectedData()
                        if (selectedData != null) copy(selectedData)
                    }
                    true
                }

                // actioMode
                R.id.select_all -> {
                    //TODO : remaining ...
                    if (currentFragment != null) {
                        currentFragment.selectAll()

                    }
                    true
                }
                else ->{
                    true
                }                }

        }

        // Called when the user exits the action mode
        override fun onDestroyActionMode(mode: ActionMode) {
            binding.contextualBottomNavigation.visibility = View.GONE
            actionMode = null
        }
    }

    private fun gotoLine() {

        var fragment: EditorFragment?
        if (isValidTab()) fragment =
            adapter.fragmentList.get(binding.tabLayout.selectedTabPosition) as EditorFragment
        else return

        val maxLine = fragment.getTotalLine()
        val startIndex = 0
            //fragment.getStartingIndexOfEdittext()
        val builder = AlertDialog.Builder(this)

        builder.setTitle("Goto Line")
        builder.setMessage("Enter line number between ($startIndex...${startIndex + maxLine - 1})")
        builder.setIcon(R.drawable.ic_goto_line)


        val view = LayoutInflater.from(this).inflate(R.layout.goto_line_dialog, null, false)
        val lineNumberEditText = view.findViewById<EditText>(R.id.line_number)

        builder.setView(view)

        builder.setPositiveButton("Find") { dialogInterface, which ->
            fragment.gotoLine(Integer.parseInt(lineNumberEditText.text.toString()) - startIndex + 1)
        }
        //performing cancel action
        builder.setNeutralButton("Cancel") { dialogInterface, which ->
            //Toast.makeText(applicationContext, "operation cancel", Toast.LENGTH_LONG).show()
            dialogInterface.dismiss()
        }

        // Create the AlertDialog
        val alertDialog: AlertDialog = builder.create()
        // Set other dialog properties
        alertDialog.setCancelable(true)
        alertDialog.show()
        if (TextUtils.isEmpty(lineNumberEditText.text)) {
            alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
        }
        lineNumberEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }

            override fun afterTextChanged(s: Editable?) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled =
                    !TextUtils.isEmpty(s) && Integer.parseInt(lineNumberEditText.text.toString()) < maxLine + startIndex && Integer.parseInt(
                        lineNumberEditText.text.toString()
                    ) >= startIndex
            }
        })

    }

    fun copy(textToCopy: String) {
        val clipboardManager = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("text", textToCopy)
        clipboardManager.setPrimaryClip(clipData)
    }

    fun feedback()
    {
        try {
            val email = Intent(Intent.ACTION_SENDTO)
            email.data = Uri.parse("mailto:nbowdeveloper@gmail.com")
            email.putExtra(Intent.EXTRA_SUBJECT, "Feedback")
            email.putExtra(Intent.EXTRA_TEXT, "Write your Feedback Here!")
            startActivity(email)
        }
        catch(e:Exception)
        {
            Toast.makeText(applicationContext,"gmail doesn't responed",Toast.LENGTH_SHORT).show()
        }


    }


    fun askForRating() {

        val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        val editor = sharedPreferences.edit()
        val opened = "opened"
        val key_got_feedback = "got_feedback"
        val num = sharedPreferences.getInt(opened,0)
        editor.putInt(opened,num+1)
        editor.commit()
        val gotFeedback = sharedPreferences.getBoolean(key_got_feedback,false)

        Log.e(TAG,"opened $num times")
        if(num>10 && !gotFeedback) {
            editor.putInt(opened,0)
            editor.commit()
            val request = manager.requestReviewFlow()
            request.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // We got the ReviewInfo object
                    val reviewInfo = task.result
                    val flow = manager.launchReviewFlow(this, reviewInfo)
                    flow.addOnCompleteListener { _ ->
                         editor.putBoolean(key_got_feedback,true)
                        Log.e(TAG, "feedback: finished")
                    }
                } else {

                    val manager2 = FakeReviewManager(applicationContext)
                    val request2 = manager2.requestReviewFlow()
                    request2.addOnCompleteListener {
                        if (task.isSuccessful) {
                            // We got the ReviewInfo object
                            val reviewInfo = task.result
                            Toast.makeText(
                                applicationContext,
                                reviewInfo.toString(),
                                Toast.LENGTH_LONG
                            )
                                .show()

                            val flow = manager2.launchReviewFlow(this, reviewInfo)
                            flow.addOnCompleteListener { _ ->

                                Log.e(TAG, "feedback: finished")
                            }
                            //Toast.makeText(applicationContext,"Internal Testing version",Toast.LENGTH_LONG).show()
                        }
                        Log.e(TAG, "feedback: error")
                        // There was some problem, log or handle the error code.
//                @ReviewErrorCode val reviewErrorCode = (task.getException() as TaskException).errorCode
                    }
                }


            }
        }


    }
}