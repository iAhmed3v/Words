package com.example.wordsapp

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.core.content.ContextCompat
import androidx.lifecycle.asLiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wordsapp.data.SettingsDataStore
import com.example.wordsapp.databinding.FragmentLetterListBinding
import kotlinx.coroutines.launch

/**
 * Entry fragment for the app. Displays a [RecyclerView] of letters.
 */
class LetterListFragment : Fragment() {

    private var _binding: FragmentLetterListBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var recyclerView: RecyclerView

    // Keeps track of which LayoutManager is in use for the [RecyclerView]
    private var isLinearLayoutManager = true

    private lateinit var settingsDataStore: SettingsDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Retrieve and inflate the layout for this fragment
        _binding = FragmentLetterListBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        recyclerView = binding.recyclerView

        // Sets the LayoutManager of the recyclerview
        // On the first run of the app, it will be LinearLayoutManager
        chooseLayout()

        // Initialize SettingsDataStore
        settingsDataStore = SettingsDataStore(requireContext())

        settingsDataStore.preferenceFlow.asLiveData().observe(viewLifecycleOwner) { value ->
            isLinearLayoutManager = value
            chooseLayout()
        }
    }


    /**
     * Sets the LayoutManager for the [RecyclerView] based on the desired orientation of the list.
     */
    private fun chooseLayout() {
        when (isLinearLayoutManager) {
            true -> {
                recyclerView.layoutManager = LinearLayoutManager(context)
                recyclerView.adapter = LetterAdapter()
            }
            false -> {
                recyclerView.layoutManager = GridLayoutManager(context, 4)
                recyclerView.adapter = LetterAdapter()
            }
        }
    }

    private fun setIcon(menuItem: MenuItem?) {
        if (menuItem == null)
            return

        // Set the drawable for the menu icon based on which LayoutManager is currently in use
        menuItem.icon =
            if (isLinearLayoutManager)
                ContextCompat.getDrawable(this.requireContext(), R.drawable.ic_grid_layout)
            else ContextCompat.getDrawable(this.requireContext(), R.drawable.ic_linear_layout)
    }

    /**
     * Determines how to handle interactions with the selected [MenuItem]
     */
    @Deprecated("Deprecated in Java")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {

            R.id.action_switch_layout -> {

                // Sets isLinearLayoutManager (a Boolean) to the opposite value
                isLinearLayoutManager = !isLinearLayoutManager

                // Launch a coroutine and write the layout setting in the preference Datastore
                lifecycleScope.launch {
                    settingsDataStore.saveLayoutToPreferencesStore(isLinearLayoutManager, requireContext())
                }
                // Sets layout and icon
                chooseLayout()
                setIcon(item)

                return true
            }

            //  Otherwise, do nothing and use the core event handling

            // when clauses require that all possible paths be accounted for explicitly,
            //  for instance both the true and false cases if the value is a Boolean,
            //  or an else to catch all unhandled cases.
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.layout_menu, menu)

        val layoutButton = menu.findItem(R.id.action_switch_layout)
        setIcon(layoutButton)
    }

    /**
     * Frees the binding object when the Fragment is destroyed.
     */
    override fun onDestroy() {
        super.onDestroy()

        _binding = null
    }
}