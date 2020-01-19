// Create a Checkout Session with the selected plan ID
var createCheckoutSession = function(param) {
  return fetch("/create-checkout-session", {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({
      village: param
    })
  }).then(handleResult);
};

// Handle any errors returned from Checkout
var handleResult = function(result) {
  if (result.error) {
    var displayError = document.getElementById("error-message");
    displayError.textContent = result.error.message;
  } else {
    return result.json();
  }
};

/* Get your Stripe publishable key to initialize Stripe.js */
fetch("/setup")
  .then(function(result) {
    return result.json();
  })
  .then(function(json) {
    var publicKey = json.publicKey;
    var stripe = Stripe(publicKey);

    // Setup event handler to create a Checkout Session when button is clicked
    document
      .getElementById("checkout-button")
      .addEventListener("click", function(evt) {
        var village = document.getElementById("village-input").value
        console.log(village)
        createCheckoutSession(village).then(function(data) {
          // Call Stripe.js method to redirect to the new Checkout page
          stripe
            .redirectToCheckout({
              sessionId: data.sessionId
            })
            .then(handleResult);
        });
      });
  });
