// Create a Checkout Session with the selected plan ID
var createCheckoutSession = function(name_, email_, village_, plan_) {
  return fetch("/create-checkout-session", {
    method: "POST",
    headers: {
      "Content-Type": "application/json"
    },
    body: JSON.stringify({
      name: name_,
      email: email_,
      village: village_,
      plan: plan_
    })
  }).then(handleResult);
};

// Handle any errors returned from Checkout
var handleResult = function(result) {
  if (result.error) {
    var displayError = document.getElementById("error-message");
    displayError.textContent = "エラーが発生しました: " + result.error.message;
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
    var plan1 = json.plan1;
    var plan2 = json.plan2;

    // Setup event handler to create a Checkout Session when button is clicked
    document
      .getElementById("checkout-button-1")
      .addEventListener("click", function(evt) {
        var name = document.getElementById("name-input").value
        var email = document.getElementById("email-input").value
        var village = document.getElementById("village-input").value
        createCheckoutSession(name, email, village, plan1).then(function(data) {
          // Call Stripe.js method to redirect to the new Checkout page
          stripe
            .redirectToCheckout({
              sessionId: data.sessionId
            })
            .then(handleResult);
        });
      });

    document
      .getElementById("checkout-button-2")
      .addEventListener("click", function(evt) {
        var name = document.getElementById("name-input").value
        var email = document.getElementById("email-input").value
        var village = document.getElementById("village-input").value
        createCheckoutSession(name, email, village, plan2).then(function(data) {
          // Call Stripe.js method to redirect to the new Checkout page
          stripe
            .redirectToCheckout({
              sessionId: data.sessionId
            })
            .then(handleResult);
        });
      });
  });
