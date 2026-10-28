import 'package:flutter_test/flutter_test.dart';
import 'package:sms_advanced/src/sms_recipient.dart';

void main() {
  test('normalizes a single SMS recipient', () {
    expect(normalizeSmsRecipient(' +1 (555) 000-1234 '), '+15550001234');
    expect(normalizeSmsRecipient('(03) 1234-5678'), '0312345678');
  });

  test('rejects URI and multiple-recipient delimiters', () {
    for (final address in ['+15550001,+15550002', '555?body=x', '555#x']) {
      expect(() => normalizeSmsRecipient(address), throwsArgumentError);
    }
  });
}
