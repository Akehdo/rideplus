provider "digitalocean" {
  token = var.do_token
}

resource "digitalocean_droplet" "rideplus" {
  name   = "rideplus-prod"
  region = var.region
  size   = var.droplet_size
  image  = "ubuntu-22-04-x64"

  ssh_keys = [var.ssh_fingerprint]


  tags = ["rideplus", "prod"]
}

