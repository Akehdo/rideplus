variable "do_token" {
  type = string
  description = "DigitalOcean API token"
  sensitive = true
}

variable "ssh_fingerprint" {
  type = string
  description = "SSH key fingerprint from DigitalOcean"
}

variable "region" {
  default = "fra1"
}

variable "droplet_size" {
  default = "s-2vcpu-2gb"
}