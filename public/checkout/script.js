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
    alert("エラーが発生しました: " + result.error.message);
  } else {
    return result.json();
  }
};

/* get villages */
fetch("https://script.google.com/macros/s/AKfycbyhGrl64tZqKdzQV2sINJUlloeCmaZt9KKD-QsCg_sRUXZrnQg/exec?action=villages")
  .then(function(result) {
    return result.json();
  })
  .then(function(json) {
    var select = document.getElementById('villages');
    json.villages.map(function(v) {
        var option = document.createElement('option');
        option.value = v;
        option.appendChild(document.createTextNode(v));
        select.appendChild(option);
    });
  });

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
        var village = document.getElementById("village").value
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
        var village = document.getElementById("village").value
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
