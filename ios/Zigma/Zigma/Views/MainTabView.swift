import SwiftUI

struct MainTabView: View {
    @State private var selectedTab = 0

    var body: some View {
        TabView(selection: $selectedTab) {
            HomeView()
                .tag(0)
                .tabItem {
                    Label("홈", systemImage: "house")
                }

            MapEntryView(selectedTab: $selectedTab)
                .tag(1)
                .tabItem {
                    Label("지도", systemImage: "map")
                }

            CreatePromiseView(selectedTab: $selectedTab)
                .tag(2)
                .tabItem {
                    Label("약속", systemImage: "plus.circle")
                }

            AccountView()
                .tag(3)
                .tabItem {
                    Label("계정", systemImage: "person")
                }
        }
        .tint(Color.zigmaBlue)
    }
}
