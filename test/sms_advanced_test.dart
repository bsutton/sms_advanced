import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:sms_advanced/sms_advanced.dart';

void main() {
  const MethodChannel channel =
      MethodChannel('plugins.elyudde.com/querySMS', JSONMethodCodec());
  SmsQuery smsQuery = SmsQuery();

  TestWidgetsFlutterBinding.ensureInitialized();
  final messenger =
      TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger;

  setUp(() {
    messenger.setMockMethodCallHandler(channel, (MethodCall methodCall) async {
      return [];
    });
  });

  tearDown(() {
    messenger.setMockMethodCallHandler(channel, null);
  });

  test('getAllSms', () async {
    expect(await smsQuery.getAllSms, []);
  });

  test('ignores SMS status from another app session', () async {
    final messenger =
        TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger;
    const sendChannel =
        MethodChannel('plugins.elyudde.com/sendSMS', JSONMethodCodec());
    const statusChannel =
        MethodChannel('plugins.elyudde.com/statusSMS', JSONMethodCodec());
    late Map<dynamic, dynamic> sentArguments;
    messenger.setMockMethodCallHandler(sendChannel, (call) async {
      sentArguments = call.arguments as Map<dynamic, dynamic>;
      return null;
    });
    messenger.setMockMethodCallHandler(statusChannel, (call) async => null);
    addTearDown(() {
      messenger.setMockMethodCallHandler(sendChannel, null);
      messenger.setMockMethodCallHandler(statusChannel, null);
    });

    final sender = SmsSender();
    final message = SmsMessage('5551234', 'test');
    await sender.sendSms(message);
    expect(sentArguments['sessionId'], matches(RegExp(r'^[0-9a-f]{32}$')));

    await messenger.handlePlatformMessage(
      'plugins.elyudde.com/statusSMS',
      const JSONMethodCodec().encodeSuccessEnvelope({
        'sentId': sentArguments['sentId'],
        'sessionId': 'wrong-session',
        'state': 'delivered',
      }),
      (_) {},
    );
    await Future<void>.delayed(Duration.zero);
    expect(message.state, SmsMessageState.Sending);

    await messenger.handlePlatformMessage(
      'plugins.elyudde.com/statusSMS',
      const JSONMethodCodec().encodeSuccessEnvelope({
        'sentId': sentArguments['sentId'],
        'sessionId': sentArguments['sessionId'],
        'state': 'delivered',
      }),
      (_) {},
    );
    await Future<void>.delayed(Duration.zero);
    expect(message.state, SmsMessageState.Delivered);
  });
}
