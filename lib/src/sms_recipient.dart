/// Returns a single phone number for an `sms:` URI.
///
/// URI separators and recipient-list delimiters must not come from an address.
String normalizeSmsRecipient(String address) {
  final value = address.trim();
  if (!RegExp(r'^[+0-9 ()-]+$').hasMatch(value)) {
    throw ArgumentError('Expected a single SMS phone number.');
  }
  final number = value.replaceAll(RegExp(r'[ ()-]'), '');
  if (!RegExp(r'^\+?[0-9]+$').hasMatch(number)) {
    throw ArgumentError('Expected a single SMS phone number.');
  }
  return number;
}
