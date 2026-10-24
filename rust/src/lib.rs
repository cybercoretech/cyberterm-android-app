use winit::platform::android::activity::AndroidApp;

#[no_mangle]
fn android_main(app: AndroidApp) {
    cyberterm_engine::run_android(app);
}
