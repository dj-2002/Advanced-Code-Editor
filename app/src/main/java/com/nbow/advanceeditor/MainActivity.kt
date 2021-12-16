package com.nbow.advanceeditor

import android.app.Activity
import android.content.*
import android.net.Uri
import android.os.Build
import android.os.Bundle
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
import android.widget.Toast

import com.google.android.material.tabs.TabLayoutMediator


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
        //MobileAds.initialize(this) {}

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
        v.setOnClickListener {
            try {
//                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
//                    addCategory(Intent.CATEGORY_OPENABLE)
//                    type = "*/*"
//                    putExtra(Intent.EXTRA_TITLE, "new.txt")
//                }
//                newFileLauncher.launch(intent)

                makeBlankFragment("untitled")

            } catch (e: Exception) {
                Toast.makeText(applicationContext, "${e.message.toString()}", Toast.LENGTH_SHORT)
                    .show()
                Log.e(TAG, "newFileLauncher: ${e.toString()}.")
            }

        }
        val v2:View = binding.noTabLayout.cl2
        v2.setOnClickListener {
            if (!helper.isStoragePermissionGranted()) helper.takePermission()

            if (helper.isStoragePermissionGranted()) chooseFile()

        }
        manager = ReviewManagerFactory.create(applicationContext)


        setDefaultToolbarTitle()
        toolbar.apply {
            setNavigationIcon(R.drawable.ic_navigation)
        }

        setSupportActionBar(toolbar)
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                changeNoTabLayout()
                toolbar.apply {
                    if (isValidTab()) {
                        if (tab != null) {
                            (adapter.fragmentList.get(tab.position)).apply {
                                title = getFileName()
                                subtitle = ""
                            }
                        }
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}

        })

        TabLayoutMediator(binding.tabLayout, binding.pager2) { tab, position ->
            (adapter.fragmentList[position]).apply{
                var fileName = getFileName()
                if(hasUnsavedChanges.value == true) fileName = "*$fileName"

                tab.apply {
                    if (customView == null) {
                        setCustomView(R.layout.tab_layout)
                    }
                    customView!!.findViewById<TextView>(R.id.file_name).setText(fileName)
                }
            }
        }.attach()


        binding.bottamBarLayout.apply {

            close.setOnClickListener {
                getCurrentFragment()?.apply{
                    if (hasUnsavedChanges.value == true) {
                        showUnsavedDialog(this)
                    } else {
                        closeTab()
                    }
                }
            }

            open.setOnClickListener {

                if (!helper.isStoragePermissionGranted()) helper.takePermission()
                if (helper.isStoragePermissionGranted()) chooseFile()
            }

            save.setOnClickListener {
                getCurrentFragment()?.apply{
                    if (hasUnsavedChanges.value != false) {
                        if(this.getFilePath().equals("note") || this.getFileName().equals("untitled")) {
                            Log.e(TAG, "save: to save note file ", )
                            saveIntentForUntitledFile(this)
                        }
                        else
                            saveFile(this, this.getUri())
                    } else
                        Toast.makeText(
                            this@MainActivity,
                            "No Changes Found",
                            Toast.LENGTH_SHORT
                        ).show()
                }
            }
            search.setOnClickListener {
                getCurrentFragment()?.apply {
                    search(this, false)
                }
            }

            toolbox.setOnClickListener {
                binding.specialCharLayout.specialCharKeyboarLayout.apply {
                    if (visibility == View.VISIBLE) visibility = View.GONE
                    else visibility = View.VISIBLE
                }
            }

            undoChange.setOnClickListener {
                getCurrentFragment()?.undoChanges()
            }
            redoChange.setOnClickListener {
                getCurrentFragment()?.redoChanges()
            }

            changeSyntax.setOnClickListener {
                getCurrentFragment()?.apply{
                    syntaxSelectionPopUp(this)
                }
            }

        }
        binding.contextualBottomNavigation.setOnItemSelectedListener(object :
            NavigationBarView.OnItemSelectedListener {
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                val currentFragment = getCurrentFragment()

                if (currentFragment != null)
                    return when (item.itemId) {

                        R.id.up -> {
                            if (indexList.isNotEmpty()) {
                                index = currentFragment.highlight(
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
                            down(currentFragment)
//                            Toast.makeText(this@MainActivity, "down", Toast.LENGTH_SHORT).show()
                            true
                        }
                        R.id.replace -> {
                            index = currentFragment.findReplace(
                                findText,
                                replaceText,
                                index,
                                ignoreCase
                            )
                            down(currentFragment)
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



        binding.specialCharLayout.charToolbar.setOnMenuItemClickListener {
            var currentFragment: EditorFragment? = getCurrentFragment()

            if (currentFragment != null)
                when (it.itemId) {
                    R.id.tab -> {
                        currentFragment.insertSpecialChar("    ")
                    }
                    R.id.roundOpen -> {
                        currentFragment.insertSpecialChar("()")
                        currentFragment.selectionPrevPosition()

                    }
                    R.id.roundClose -> {
                        currentFragment.insertSpecialChar(")")
                    }
                    R.id.breaketOpen -> {
                        currentFragment.insertSpecialChar("[]")
                        currentFragment.selectionPrevPosition()
                    }
                    R.id.breaketClose -> {
                        currentFragment.insertSpecialChar("]")
                    }
                    R.id.curlyOpen -> {
                        currentFragment.insertSpecialChar("{}")
                        currentFragment.selectionPrevPosition()

                    }
                    R.id.curlyClose -> {
                        currentFragment.insertSpecialChar("}")
                    }
                    R.id.empercent -> {
                        currentFragment.insertSpecialChar("&")
                    }
                    R.id.singlequote -> {
                        currentFragment.insertSpecialChar("\'")
                    }
                    R.id.doublequote -> {
                        currentFragment.insertSpecialChar("\"")
                    }
                    R.id.dollor -> {
                        currentFragment.insertSpecialChar("$")
                    }
                    R.id.greaterThan -> {
                        currentFragment.insertSpecialChar(">")
                    }
                    R.id.lessThan -> {
                        currentFragment.insertSpecialChar("<")
                    }
                    R.id.exclamation -> {
                        currentFragment.insertSpecialChar("!")
                    }
                    R.id.colon -> {
                        currentFragment.insertSpecialChar(":")
                    }
                    R.id.semi_colon -> {
                        currentFragment.insertSpecialChar(";")
                    }
                    R.id.question_mark -> {
                        currentFragment.insertSpecialChar("?")
                    }
                    R.id.forward_slash -> {
                        currentFragment.insertSpecialChar("/")
                    }
                    R.id.at_the_rate -> {
                        currentFragment.insertSpecialChar("@")
                    }
                    R.id.pipeline_symbol -> {
                        currentFragment.insertSpecialChar("|")
                    }

                }
            false
        }

    }

    private fun syntaxSelectionPopUp(currentFragment: EditorFragment) {

        var view = binding.bottamBarLayout.changeSyntax as View

        val popup = android.widget.PopupMenu(applicationContext, view)
        popup.inflate(R.menu.syntax_menu)

        popup.setOnMenuItemClickListener { item ->


            when (item.itemId) {

                R.id.no_syntax -> {
                    currentFragment.applySynatx("no")
                }

                R.id.default_syntax -> {
                    currentFragment.applySynatx("default")
                }
                R.id.java -> {
                    currentFragment.applySynatx(".java")
                }
                R.id.php -> {
                    currentFragment.applySynatx(".php")
                }
                R.id.c -> {
                    currentFragment.applySynatx(".c")
                }
                R.id.cpp -> {
                    currentFragment.applySynatx(".cpp")
                }
                R.id.python -> {
                    currentFragment.applySynatx(".py")
                }
                R.id.html -> {
                    currentFragment.applySynatx(".html")
                }
                R.id.css -> {
                    currentFragment.applySynatx(".css")
                }
                R.id.javascript -> {
                    currentFragment.applySynatx(".js")
                }
                R.id.go -> {
                    currentFragment.applySynatx(".go")
                }
                R.id.xml -> {
                    currentFragment.applySynatx(".xml")
                }
                R.id.plain_text -> {
                    currentFragment.applySynatx(".txt")
                }


            }
            false

        }

        popup.show()


    }

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
//            R.id.nav_ad -> {
//
//                if (Admob.rewardedAd != null) {
//                    Log.e(TAG, "onNavigationItemSelected: admob initialized", )
//                    Admob.rewardedAd!!.show(
//                        this,
//                        OnUserEarnedRewardListener { rewardItem ->
//                            Toast.makeText(
//                                this@MainActivity,
//                                "Rewarded with Thanks", Toast.LENGTH_SHORT
//                            ).show()
//                        })
//                }
//
//            }

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
//        binding.tabLayout.apply {
//            if (isValidTab()) {
//                adapter.apply {
//                    fragmentList.removeAt(selectedTabPosition)
//                    notifyItemRemoved(selectedTabPosition)
//                }
//                removeTabAt(selectedTabPosition)
//                if (tabCount == 0) {
//                    //setDefaultToolbarTitle()
//                }
//            }
//        }
        removeCurrentFragment()
        if(binding.tabLayout.tabCount==0) setDefaultToolbarTitle()
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

        val keyIsThemeChanged = "is_theme_changed_setting"
        val isThemeChangedFromSetting = preferences.getBoolean(keyIsThemeChanged, false)
        if (wrap != model.isWrap) {
            model.isWrap = wrap
            recreate()
        }
        darkTheme = preferences.getBoolean(THEME_PREFERENCE_KEY, true)
        changeTheme()

        model.isHistoryLoaded.observe(this@MainActivity) {
            adapter.fragmentList = model.getFragmentList().value ?: arrayListOf()
            adapter.notifyDataSetChanged()
            if(model.currentTabIndex>=0 && model.currentTabIndex<adapter.fragmentList.size)
                binding.pager2.currentItem = model.currentTabIndex


            binding.tabLayout.apply {
                if(it && adapter.fragmentList.size==0){
                    makeBlankFragment("untitled")
                }
            }

            //createTabsInTabLayout(adapter.fragmentList)

            for ((count, frag) in model.getFragmentList().value!!.withIndex()) {
                frag.hasUnsavedChanges.observe(this@MainActivity) {
                    var fileName = frag.getFileName()
                    if (it) fileName = "*$fileName"
                    setCustomTabLayout(count, fileName)
                }
                frag.hasLongPress.observe(this@MainActivity) {
                    if (it) {
                        startActionMode(actionModeCallbackCopyPaste)
                        frag.hasLongPress.value = false
                    }
                }
            }
        }

        if (binding.tabLayout.tabCount > 0 && isThemeChangedFromSetting) {

            val editor = preferences.edit()
            // this was commit
            editor.putBoolean(keyIsThemeChanged, false)
            editor.apply()
        }



        Log.e(TAG, "onResume: called")
//        if(model.currentTabIndex>=0 && model.currentTabIndex<adapter.fragmentList.size)
//            binding.pager2.currentItem = model.currentTabIndex

    }


//    private fun makeTabLayoutTitleToUnsaved() {
//        val position = binding.tabLayout.selectedTabPosition
//        getCurrentFragment()?.apply {
//            hasUnsavedChanges.value = true
//            binding.tabLayout.getTabAt(position)?.text = "*${getFileName()}"
//        }
//    }

    private fun getCurrentFragment() : EditorFragment?{
        val position = binding.tabLayout.selectedTabPosition
//        val position = pager2.currentItem
        if(position>=0)
            return (adapter.fragmentList[position] as EditorFragment)

        return null
    }


    private fun removeCurrentFragment() {
        val position = binding.pager2.currentItem
        adapter.fragmentList.removeAt(position)
        adapter.notifyItemRemoved(position)
    }

    private fun addFragment(fragment : EditorFragment){
        val position = binding.tabLayout.tabCount
        adapter.fragmentList.add(fragment)
        adapter.notifyItemInserted(position)
        binding.pager2.currentItem = position
        adapter.notifyItemChanged(position)

        fragment.hasUnsavedChanges.observe(this@MainActivity) {
            var fileName = fragment.getFileName()
            if(it) fileName = "*$fileName"
            setCustomTabLayout(position, fileName)
        }

        fragment.hasLongPress.observe(this@MainActivity){
            if(it) {
                startActionMode(actionModeCallbackCopyPaste)
                fragment.hasLongPress.value = false
            }
        }

    }




    fun makeBlankFragment(fileName: String)
    {

        Log.e(TAG, "makeBlankFragment: " )
        val list:MutableList<StringBuilder> = arrayListOf()
        list.add(StringBuilder(""))
        val dataFile = DataFile(
            fileName = fileName,
            filePath = "note",
            uri = Uri.parse(""),
            list
        )
        val fragment = EditorFragment(dataFile,hasUnsavedChanges = true)

        addFragment(fragment)

//        fragment.hasUnsavedChanges.observe(this@MainActivity) {
//            if (it) {
//                setCustomTabLayout(binding.tabLayout.tabCount-1, "*${fragment.getFileName()}")
//            }else setCustomTabLayout(binding.tabLayout.tabCount-1, fragment.getFileName())
//        }
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
        model.currentTabIndex = binding.pager2.currentItem


        for (frag in adapter.fragmentList) {
            val fragment = frag
            fragment.saveDataToPage()
        }
        model.addHistories(applicationContext)
    }

    override fun onDestroy() {

        // saving all files to databse
        model.setFragmentList(adapter.fragmentList)

        // saving current tab

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


        popup.setOnMenuItemClickListener { item -> //TODO : list all action for menu popup
//            Log.e(TAG, "onMenuItemClick: " + item.title)
            var currentFragment: EditorFragment? = getCurrentFragment()

            when (item.itemId) {

                R.id.open -> {

                    if (!helper.isStoragePermissionGranted()) helper.takePermission()

                    if (helper.isStoragePermissionGranted()) chooseFile()
                }
                R.id.save_as -> {
                    saveAsIntent(currentFragment)
                }
                R.id.save -> {
                    if (currentFragment != null) {
                        if (currentFragment.hasUnsavedChanges.value != false) {
                            if(currentFragment.getFilePath().equals("note"))
                                saveIntentForUntitledFile(currentFragment)
                            else
                                saveFile(currentFragment, currentFragment.getUri())
                        }
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

//                        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
//                            addCategory(Intent.CATEGORY_OPENABLE)
//                            type = "*/*"
//                            putExtra(Intent.EXTRA_TITLE, "new.txt")
//                        }
//                        newFileLauncher.launch(intent)
                        makeBlankFragment("untitled")
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
//                R.id.go_to_line -> {
//                    //TODO : remaining ...
//                    gotoLine()
//                }
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
            val listOfPageData: MutableList<StringBuilder> = arrayListOf()

            bufferedReader.forEachLine {
                listOfLines.add(it)
            }

            var temp = StringBuilder("")
            var count = 0
            for (line in listOfLines) {
                temp.append(line)
                count++
                if (count >= 500 || temp.length >= 500000) {
                    listOfPageData.add(temp)
                    count = 0
                    temp = StringBuilder()
                } else temp.append("\n")
            }
            if (temp.isNotEmpty()) {
                listOfPageData.add(temp)
            }
            if (listOfLines.size == 0) {
                listOfPageData.add(temp)
            }
                
            for(page in listOfPageData)
            {
                Log.e(TAG, "readFileUsingUri: page ${page.length}", )
            }

            Log.e(TAG, "readFileUsingUri: listofpages size ${listOfPageData.size}", )

            val fileName: String = helper.queryName(contentResolver, uri)
            val dataFile = DataFile(
                fileName = fileName,
                filePath = uri.path!!,
                uri = uri,
                listOfPageData = listOfPageData
            )
            val fragment = EditorFragment(dataFile)

            if ((isReload) && isValidTab()) {

                Log.e(TAG, "readFileUsingUri: removing and adding fragment is Reload ", )
                val position = binding.pager2.currentItem
                adapter.fragmentList.removeAt(position)
                adapter.fragmentList.add(position, fragment)
                adapter.notifyItemChanged(position)
                setCustomTabLayout(position, "$fileName")
//                adapter.notifyDataSetChanged()

            } else {

                addFragment(fragment)
                if (isOuterFile)
                    model.getFragmentList().value?.add(fragment)


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
//            fragment.hasUnsavedChanges.observe(this) {
//                if (it)
//                    setCustomTabLayout(binding.tabLayout.selectedTabPosition, "*$fileName")
//                else setCustomTabLayout(binding.tabLayout.selectedTabPosition, fileName)
//            }
//            fragment.hasLongPress.observe(this@MainActivity){
//                if(it) {
//                    startActionMode(actionModeCallbackCopyPaste)
//                    fragment.hasLongPress.value = false
//                }
//            }
//            Log.e(
//                TAG,
//                "readFileUsingUri : tab layout selected position : ${binding.tabLayout.selectedTabPosition}"
//            )
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

//    private fun createTabsInTabLayout(list: MutableList<EditorFragment>) {
//        binding.tabLayout.removeAllTabs()
//
//        if (binding.tabLayout.tabCount == 0) {
//
//            binding.tabLayout.apply {
//                list.forEach {
//                    val frag = it
//                    addTab(newTab())
//                    setCustomTabLayout(tabCount - 1, frag.getFileName())
//                }
//            }
//
//        }
////        if (model.currentTab >= 0 && model.currentTab < adapter.fragmentList.size) {
////            binding.pager2.setCurrentItem(model.currentTab)
////        }
//        binding.pager2.currentItem = model.currentTabIndex
//        adapter.notifyDataSetChanged()
//    }

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
                    getCurrentFragment()?.apply{
                        saveFile(this, uri, isSaveAs = true)
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
    )
    {
//        val uri = fragment.getUri()
        if (uri !== null) {
            try {
                val takeFlags: Int =
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
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

                val fileExtension = currentFragment.getFileExtension()
                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                    addCategory(Intent.CATEGORY_OPENABLE)
                    type = "*/*"
                    putExtra(Intent.EXTRA_TITLE, "untitled${fileExtension}")
                }
                saveAsSystemPickerLauncher.launch(intent)
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
                    adapter.fragmentList.get(binding.tabLayout.selectedTabPosition)
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
                    adapter.fragmentList.get(binding.tabLayout.selectedTabPosition)
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
            adapter.fragmentList.get(binding.tabLayout.selectedTabPosition)
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
    private fun saveIntentForUntitledFile(currentFragment: EditorFragment) {

        try {

            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
                putExtra(Intent.EXTRA_TITLE, "untitled.java")
            }
            saveSystemPickerLauncherForUntitled.launch(intent)
        }
        catch (e:Exception)
        {
            Toast.makeText(applicationContext, "${e.message.toString()}", Toast.LENGTH_SHORT).show()
            Log.e(TAG, "saveAsIntent: ${e.toString()}.")
        }

    }
    val saveSystemPickerLauncherForUntitled =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val intent: Intent? = result.data
                val uri: Uri? = intent?.data
                if (uri != null) {
//                    Log.e(TAG, "save as sytem picker: uri -> $uri")

                    getCurrentFragment()?.apply {
                        setUri(uri)
                        saveFile(this, uri)
                        reloadFile(this)
                    }
                    //readFileUsingUri(uri,isUntitled = true)

                }
            }
        }

}