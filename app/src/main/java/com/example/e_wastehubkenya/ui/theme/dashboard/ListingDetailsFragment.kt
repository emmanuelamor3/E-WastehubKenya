package com.example.e_wastehubkenya.ui.theme.dashboard

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.e_wastehubkenya.R
import com.example.e_wastehubkenya.data.Resource
import com.example.e_wastehubkenya.data.model.CartItem
import com.example.e_wastehubkenya.data.model.Listing
import com.example.e_wastehubkenya.data.model.User
import com.example.e_wastehubkenya.databinding.FragmentListingDetailsBinding
import com.example.e_wastehubkenya.viewmodel.CartViewModel
import com.example.e_wastehubkenya.viewmodel.ListingViewModel
import com.example.e_wastehubkenya.viewmodel.UserViewModel
import com.google.firebase.auth.FirebaseAuth

class ListingDetailsFragment : Fragment() {

    private var _binding: FragmentListingDetailsBinding? = null
    private val binding get() = _binding!!

    private val userViewModel: UserViewModel by viewModels()
    private val cartViewModel: CartViewModel by viewModels()
    private val listingViewModel: ListingViewModel by viewModels()
    private lateinit var auth: FirebaseAuth
    private var currentListing: Listing? = null
    private var seller: User? = null
    private lateinit var productImageAdapter: ProductImageAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListingDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        (activity as AppCompatActivity).setSupportActionBar(binding.toolbar)
        (activity as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        val listingArg = arguments?.getParcelable<Listing>("listing")
        listingArg?.let {
            currentListing = it
            userViewModel.fetchUserById(it.userId)
            listingViewModel.incrementViewCount(it.id, auth.currentUser?.uid ?: "")
            listingViewModel.getListing(it.id)
        }

        userViewModel.fetchUser()

        binding.btnAddToCart.setOnClickListener {
            currentListing?.let { addToCart(it) }
        }

        binding.btnContactSeller.setOnClickListener {
            showDisclaimerAndNavigate()
        }

        binding.btnDeleteListing.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        binding.btnPreviousImage.setOnClickListener {
            binding.viewPagerImages.currentItem = binding.viewPagerImages.currentItem - 1
        }

        binding.btnNextImage.setOnClickListener {
            binding.viewPagerImages.currentItem = binding.viewPagerImages.currentItem + 1
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        userViewModel.seller.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) {
                seller = resource.data
                seller?.let { bindSellerDetails(it) }
            }
        }

        userViewModel.user.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) {
                currentListing?.let { bindListingDetails(it, resource.data) }
            }
        }

        listingViewModel.listing.observe(viewLifecycleOwner) { resource ->
            if (resource is Resource.Success) {
                val listing = resource.data
                if (listing != null) {
                    currentListing = listing
                    bindListingDetails(listing, userViewModel.user.value?.data)
                }
            }
        }

        cartViewModel.addToCartResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> { /* Show loading */ }
                is Resource.Success -> {
                    Toast.makeText(context, "Added to cart!", Toast.LENGTH_SHORT).show()
                }
                is Resource.Error -> {
                    Toast.makeText(context, "Error: ${resource.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

        listingViewModel.deleteListingResult.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> { /* Show loading */ }
                is Resource.Success -> {
                    Toast.makeText(context, "Listing deleted successfully", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                is Resource.Error -> {
                    Toast.makeText(context, "Error: ${resource.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun showDisclaimerAndNavigate() {
        AlertDialog.Builder(requireContext())
            .setTitle("Disclaimer")
            .setMessage("Please only contact the seller if you have shortlisted this product to buy. All products are sold on a first-come, first-served basis.")
            .setPositiveButton("Proceed") { _, _ ->
                seller?.let {
                    val bundle = Bundle().apply {
                        putString("sellerId", it.uid)
                        putString("listingId", currentListing?.id)
                    }
                    findNavController().navigate(R.id.action_listingDetailsFragment_to_chatFragment, bundle)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun bindListingDetails(listing: Listing, currentUser: User?) {
        productImageAdapter = ProductImageAdapter(listing.imageUrls)
        binding.viewPagerImages.adapter = productImageAdapter

        binding.tvProductName.text = listing.productName
        binding.tvDescription.text = listing.description
        binding.tvCategory.text = listing.category
        binding.tvBrand.text = listing.brand
        binding.tvModel.text = listing.model
        binding.tvCondition.text = listing.condition
        binding.tvLocation.text = listing.location
        binding.tvViewCount.text = "${listing.viewCount} Views"

        val isSeller = currentUser?.uid == listing.userId

        if (isSeller) {
            binding.tvViewCount.visibility = View.VISIBLE
            binding.btnDeleteListing.visibility = View.VISIBLE
            binding.btnAddToCart.visibility = View.GONE
            binding.btnContactSeller.visibility = View.GONE
            binding.tvSellerPhoneNumber.visibility = View.GONE
            binding.tvLocation.visibility = View.VISIBLE

            if (listing.isDonation) {
                binding.tvPrice.visibility = View.GONE
            } else {
                binding.tvPrice.visibility = View.VISIBLE
                binding.tvPrice.text = String.format("Ksh %.2f", listing.price)
            }
        } else {
            binding.tvViewCount.visibility = View.GONE
            binding.btnDeleteListing.visibility = View.GONE
            binding.tvSellerPhoneNumber.visibility = View.VISIBLE

            if (listing.isDonation) {
                binding.tvPrice.visibility = View.GONE
                binding.tvLocation.visibility = View.VISIBLE
                binding.btnAddToCart.visibility = View.GONE
                binding.btnContactSeller.visibility = View.VISIBLE
            } else {
                binding.tvPrice.visibility = View.VISIBLE
                binding.tvPrice.text = String.format("Ksh %.2f", listing.price)
                binding.tvLocation.visibility = View.VISIBLE
                binding.btnAddToCart.visibility = View.VISIBLE
                binding.btnContactSeller.visibility = View.VISIBLE
                binding.btnAddToCart.isEnabled = true
            }
        }
    }

    private fun bindSellerDetails(seller: User) {
        binding.tvSellerName.text = seller.name
        binding.tvSellerPhoneNumber.text = seller.phoneNumber
        if (seller.profilePictureUrl.isNotEmpty()) {
            binding.ivSellerImage.load(seller.profilePictureUrl)
        }
    }

    private fun addToCart(listing: Listing) {
        val cartItem = CartItem(
            listingId = listing.id,
            sellerId = listing.userId,
            productName = listing.productName,
            price = listing.price,
            imageUrl = listing.imageUrls.firstOrNull() ?: ""
        )
        cartViewModel.addToCart(cartItem)
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Listing")
            .setMessage("Are you sure you want to delete this listing?")
            .setPositiveButton("Delete") { _, _ ->
                currentListing?.let { listingViewModel.deleteListing(it) }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
